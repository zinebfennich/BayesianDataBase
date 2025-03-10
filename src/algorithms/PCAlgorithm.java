package algorithms;

import database.DatabaseUtils;
import structures.*;
import utils.Combiner;
import utils.PCAlgorithmUtils;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Contient l'implémentation de l'algorithme PC pour la découverte causale.
 */
public class PCAlgorithm {
    private String tableName;
    private Graph graph = new Graph();
    private double threshold = 0.3; //seuil de corrélation ici fixé à 30%

    public void discoverCausalStructure(Connection connection, String[] columns) {
        //1) créer un réseau bayésiens complet non orienté
        initializeGraph(connection);
        //2) calculer les INDEPENDANCES CONDITIONNELLES D ORDRE 1 (A et B) entre les paires de variables
        calculateVariablePairCorrelations(connection);
        //3) supprimer les liens entre les noeuds de t_edges si la correlation < seuil ou correlation_exists = false
        removeT_edgesCorrelationEdges(connection);
        //4) calculer les INDEPENDANCE CONDITIONNELLE D'ORDRE 1 (A et B | C)
        //il faut choisir ici d'utiliser Pearson ou khi2
        //il faut modifier testcall3 pour choisir quel test utiliser
        calculatePartialCorrelations(connection);
        //5) enlever les liens entre les variables qui sont correlées mais deviennent non correlés dans t_edges_ci1
        removeConditionallyIndependentEdges(connection);
    }

    /**
     * Fonction qui initialise un graphe complet non orienté avec comme noeuds
     * les variables de la table.
     * @param connection
     */
    private void initializeGraph(Connection connection) {
        try {
            graph.addTableNodesToGraph(connection, tableName);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        graph.createCompleteGraph();
    }

    /**
     * Fonction qui calcule les corrélations entre les paires de variables possibles de la table
     * et stocke le résultat dans t_edges.
     * @param connection
     */
    private void calculateVariablePairCorrelations(Connection connection) {
        Node[] nodesTab = getGraphNodesArray();
        Combiner<Node> combiner = new Combiner<>(2, nodesTab);
        Node[] pair = new Node[2];

        while (combiner.searchNext(pair)) {
            try {
                DatabaseUtils.testcall1(connection, tableName, pair[0].getName(), pair[1].getName());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Fonction qui met le flag correlation_exists dans t_edges à true si la valeur absolue de la
     * corrélation est supérieure au seuil, false sinon.
     * @param connection
     */
    private void updateFlagT_edges(Connection connection, double threshold) {
        String updateQuery = "UPDATE t_edges SET correlation_exists = ABS(corr) >= ?";
        try (var updateStatement = connection.prepareStatement(updateQuery)) {
            updateStatement.setDouble(1, threshold);
            updateStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour de correlation_exists dans t_edges.", e);
        }
    }


    /**
     * Fonction qui enlève le lien entre les noeuds où la corrélation est inférieure au seuil , c'est à dire
     * entre les noeuds de t_edges où correlation_exists = false.
     * @param connection
     */
    private void removeT_edgesCorrelationEdges(Connection connection) {
        updateFlagT_edges(connection, threshold);
        try (var statement = connection.createStatement();
             var resultSet = statement.executeQuery("SELECT node1, node2, correlation_exists FROM t_edges")) {

            while (resultSet.next()) {
                if (!resultSet.getBoolean("correlation_exists")) {
                    graph.removeEdge(resultSet.getString("node1"), resultSet.getString("node2"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * Fonction qui génére tous les triplets de noeuds possibles à partir des variables de la table. Elle enlève les
     * doublons car a et b | c c'est la même chose que b et a | c.
     * @param connection
     */
    private void calculatePartialCorrelations(Connection connection) {
        Node[] nodesTab = getGraphNodesArray();
        Combiner<Node> combinerTriplets = new Combiner<>(3, nodesTab);
        Node[] triplet = new Node[3];

        while (combinerTriplets.searchNext(triplet)) {
            //appeler la fonction qui va calculer les corr partielles entre les noeuds et insérer le
            //résultat dans t_edges ci1
            //pour le moment j'utilise que Pearson
            //si g bien compris il faut check au début si les var sont continues ou discrètes
            try{
                DatabaseUtils.testcall3(connection, tableName, triplet[0].getName(), triplet[1].getName(),triplet[2].getName());
            } catch (SQLException e) {
                throw new RuntimeException("Erreur lors de l'insertion de données dans t_edges_ci1.", e);
            }

        }
    }

    private void removeConditionallyIndependentEdges(Connection connection) {
        try (var statement = connection.createStatement();
             var resultSet = statement.executeQuery("SELECT node1, node2, node3, corr_part FROM t_edges_2")) {

            while (resultSet.next()) {
                if (isConditionallyIndependent(connection, resultSet)) {
                    graph.removeEdge(resultSet.getString("node1"), resultSet.getString("node2"));
                    updateCorrelationExistence(connection, resultSet);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la récupération des données dans t_edges_2.", e);
        }
    }

    private boolean isConditionallyIndependent(Connection connection, var resultSet) throws SQLException {
        String node1Name = resultSet.getString("node1");
        String node2Name = resultSet.getString("node2");
        String node3Name = resultSet.getString("node3");
        Double partialCorrelation = resultSet.getObject("corr_part", Double.class);

        if (partialCorrelation != null && Math.abs(partialCorrelation) < 0.05) {
            Node node1 = graph.getNodeByName(node1Name);
            Node node2 = graph.getNodeByName(node2Name);
            Node node3 = graph.getNodeByName(node3Name);

            if (node1.isNumeric() && node2.isNumeric() && node3.isNumeric()) {
                return PCAlgorithmUtils.performChiSquaredTestForThreeVariables(connection, tableName, node1Name, node2Name, node3Name);
            }
        }
        return false;
    }

    private void updateCorrelationExistence(Connection connection, var resultSet) throws SQLException {
        String updateQuery = "UPDATE t_edges_2 SET correlation_exists = ? WHERE node1 = ? AND node2 = ? AND node3 = ?";
        try (var updateStatement = connection.prepareStatement(updateQuery)) {
            updateStatement.setBoolean(1, false);
            updateStatement.setString(2, resultSet.getString("node1"));
            updateStatement.setString(3, resultSet.getString("node2"));
            updateStatement.setString(4, resultSet.getString("node3"));
            updateStatement.executeUpdate();
        }
    }

    private Node[] getGraphNodesArray() {
        Node[] nodesTab = new Node[graph.getNodes().size()];
        graph.getNodes().toArray(nodesTab);
        return nodesTab;
    }


}
