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

    public void discoverCausalStructure(Connection connection, String[] columns) {
        //1) créer un réseau bayésiens complet non orienté
        try {
            graph.addTableNodesToGraph(connection, tableName);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        graph.createCompleteGraph();

        //2) calculer les corrélations
        // Créez une instance de Combiner pour générer toutes les paires de colonnes (2 éléments par combinaison)
        Node[] nodesTab = new Node[graph.getNodes().size()];
        graph.getNodes().toArray(nodesTab);
        Combiner<Node> combiner = new Combiner<>(2, nodesTab);

        // Tableau pour stocker la combinaison actuelle (chaque paire de noeuds)
        Node[] paire = new Node[2];

        // Tant qu'il y a une nouvelle combinaison (paires de colonnes)
        while (combiner.searchNext(paire)) {
            Node node1 = paire[0];  // Premier noeud de la paire
            Node node2 = paire[1];  // Deuxième noeud de la paire

            String colonne1 = node1.getName();  // Récupérer le nom de la colonne du noeud 1
            String colonne2 = node2.getName();  // Récupérer le nom de la colonne du noeud 2

            // Appeler la méthode testcall1 pour insérer la paire de colonnes et calculer la corrélation
            try {
                DatabaseUtils.testcall1(connection, tableName, colonne1, colonne2);
            } catch (SQLException e) {
                e.printStackTrace();  // Gérer les erreurs SQL
            }
        }

        //3) pour chaque ligne dans t-edges , si correlation =0 alors suppression du lien entre les noeuds
        try {
            String query = "SELECT node1, node2, corr, correlation_exists FROM t_edges";
            try (var statement = connection.createStatement();
                 var resultSet = statement.executeQuery(query)) {

                while (resultSet.next()) {
                    String node1Name = resultSet.getString("node1");
                    String node2Name = resultSet.getString("node2");
                    Boolean correlationExists = resultSet.getBoolean("correlation_exists");

                    if (!correlationExists) {
                        System.out.println("Suppression du lien entre " + node1Name + " et " + node2Name + " car corr = 0");

                        Node node1 = graph.getNodeByName(node1Name);
                        Node node2 = graph.getNodeByName(node2Name);
                        if (node1 != null && node2 != null) {
                            node1.removeLink(node2);
                            node2.removeLink(node1);
                        }

                        // Supprime la ligne de `t_edges`
                        String deleteQuery = "DELETE FROM t_edges WHERE node1 = ? AND node2 = ? AND correlation_exists = FALSE";
                        try (var deleteStatement = connection.prepareStatement(deleteQuery)) {
                            deleteStatement.setString(1, node1Name);
                            deleteStatement.setString(2, node2Name);
                            deleteStatement.executeUpdate();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


// 4) Calcul des corrélations partielles pour les triplets de nœuds
        Combiner<Node> combinerTriplets = new Combiner<>(3, nodesTab);
        Node[] triplet = new Node[3];

        while (combinerTriplets.searchNext(triplet)) {
            Node node1 = triplet[0];
            Node node2 = triplet[1];
            Node node3 = triplet[2];

            String colonne1 = node1.getName();
            String colonne2 = node2.getName();
            String colonne3 = node3.getName();

            try {
                // Appel de testcall2 pour stocker la corrélation partielle dans t_edges2
                DatabaseUtils.testcall2(connection, tableName, colonne1, colonne2, colonne3);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

// 5) Suppression des liens entre les triplets de noeuds si chi-squared indique l'indépendance conditionnelle
        try {
            String query = "SELECT node1, node2, node3, corr_part FROM t_edges_2";
            try (var statement = connection.createStatement();
                 var resultSet = statement.executeQuery(query)) {

                while (resultSet.next()) {
                    String node1Name = resultSet.getString("node1");
                    String node2Name = resultSet.getString("node2");
                    String node3Name = resultSet.getString("node3");
                    Double partialCorrelation = resultSet.getObject("corr_part", Double.class);

                    // Vérifier si la corrélation partielle est faible (proche de 0)
                    if (partialCorrelation != null && Math.abs(partialCorrelation) < 0.05) {
                        Node node1 = graph.getNodeByName(node1Name);
                        Node node2 = graph.getNodeByName(node2Name);
                        Node node3 = graph.getNodeByName(node3Name);

                        // Vérifier si les variables sont numériques avant d'appliquer le test du Chi-carré
                        if (node1.isNumeric() && node2.isNumeric() && node3.isNumeric()) {
                            boolean isIndependent = PCAlgorithmUtils.performChiSquaredTestForThreeVariables(connection, tableName, node1Name, node2Name, node3Name);

                            // Si les variables sont indépendantes conditionnellement, on supprime les liens
                            if (isIndependent) {
                                System.out.println("Suppression du lien entre " + node1Name + ", " + node2Name + " et " + node3Name + " (Indépendance conditionnelle)");

                                if (node1 != null && node2 != null && node3 != null) {
                                    node1.removeLink(node2);
                                    node2.removeLink(node1);
                                    node1.removeLink(node3);
                                    node3.removeLink(node1);
                                    node2.removeLink(node3);
                                    node3.removeLink(node2);
                                }
                                String updateQuery = "UPDATE t_edges_2 SET correlation_exists = ? WHERE node1 = ? AND node2 = ? AND node3 = ?";
                                try (var updateStatement = connection.prepareStatement(updateQuery)) {
                                    boolean correlationExists = partialCorrelation != null && Math.abs(partialCorrelation) >= 0.0001;  // 🔥 Si corr_part ≈ 0, mettre FALSE

                                    updateStatement.setBoolean(1, correlationExists);
                                    updateStatement.setString(2, node1Name);
                                    updateStatement.setString(3, node2Name);
                                    updateStatement.setString(4, node3Name);

                                    int rowsUpdated = updateStatement.executeUpdate();
                                    System.out.println("🔄 Mise à jour de correlation_exists (" + correlationExists + ") pour : "
                                            + node1Name + ", " + node2Name + ", " + node3Name);
                                    System.out.println("✔ Nombre de lignes mises à jour : " + rowsUpdated);

                                    if (rowsUpdated == 0) {
                                        System.out.println("⚠ Aucune ligne mise à jour ! Vérifie si ces valeurs existent bien dans t_edges_2.");
                                    }
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }






                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }}

