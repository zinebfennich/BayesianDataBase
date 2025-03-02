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
            String query = "SELECT node1, node2, corr FROM t_edges";
            try (var statement = connection.createStatement();
                 var resultSet = statement.executeQuery(query)) {

                while (resultSet.next()) {
                    String node1Name = resultSet.getString("node1");
                    String node2Name = resultSet.getString("node2");
                    Double correlation = resultSet.getObject("corr", Double.class); // attention peut être null

                    // Vérifier si la corrélation est nulle ou égale à 0
                    if (correlation != null && correlation == 0.0) {
                        Node node1 = graph.getNodeByName(node1Name);
                        Node node2 = graph.getNodeByName(node2Name);

                        if (node1 != null && node2 != null) {
                            node1.removeLink(node2);
                            node2.removeLink(node1);
                        }

                        //NB: à demander si on supprime ou pas ces lignes de t_edges (où corr=0) ou ^pas
//                        String deleteQuery = "DELETE FROM t_edges WHERE node1 = ? AND node2 = ?";
//                        try (var deleteStatement = connection.prepareStatement(deleteQuery)) {
//                            deleteStatement.setString(1, node1Name);
//                            deleteStatement.setString(2, node2Name);
//                            deleteStatement.executeUpdate();
//                        } catch (SQLException e) {
//                            throw new RuntimeException(e);
//                        }

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

        // 8) Suppression des liens entre les triplets de noeuds qui ont corrélation partielle = 0 dans t_edges2
        try {
            String query = "SELECT node1, node2, node3, corr_part FROM t_edges_2";
            try (var statement = connection.createStatement();
                 var resultSet = statement.executeQuery(query)) {

                while (resultSet.next()) {
                    String node1Name = resultSet.getString("node1");
                    String node2Name = resultSet.getString("node2");
                    String node3Name = resultSet.getString("node3");
                    Double partialCorrelation = resultSet.getObject("corr_part", Double.class);

                    // Vérifier si la corrélation partielle est nulle ou égale à 0
                    if (partialCorrelation != null && partialCorrelation == 0.0) {
                        Node node1 = graph.getNodeByName(node1Name);
                        Node node2 = graph.getNodeByName(node2Name);
                        Node node3 = graph.getNodeByName(node3Name);

                        if (node1 != null && node2 != null && node3 != null) {
                            // Supprimer les liens entre les nœuds
                            node1.removeLink(node2);
                            node2.removeLink(node1);
                            node1.removeLink(node3);
                            node3.removeLink(node1);
                            node2.removeLink(node3);
                            node3.removeLink(node2);
                        }

//                        // A voir Supprimer les lignes de t_edges_2 où corr_part = 0
//                        String deleteQuery = "DELETE FROM t_edges_2 WHERE node1 = ? AND node2 = ? AND node3 = ?";
//                        try (var deleteStatement = connection.prepareStatement(deleteQuery)) {
//                            deleteStatement.setString(1, node1Name);
//                            deleteStatement.setString(2, node2Name);
//                            deleteStatement.setString(3, node3Name);
//                            deleteStatement.executeUpdate();
//                        } catch (SQLException e) {
//                            throw new RuntimeException(e);
//                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 8) Suppression des liens entre les triplets de nœuds qui ont corrélation partielle = 0 dans t_edges2
        try {
            String query = "SELECT node1, node2, node3, corr_part FROM t_edges_2";
            try (var statement = connection.createStatement();
                 var resultSet = statement.executeQuery(query)) {

                while (resultSet.next()) {
                    String node1Name = resultSet.getString("node1");
                    String node2Name = resultSet.getString("node2");
                    String node3Name = resultSet.getString("node3");
                    Double partialCorrelation = resultSet.getObject("corr_part", Double.class);

                    // Vérifier si la corrélation partielle est nulle ou égale à 0
                    if (partialCorrelation != null && partialCorrelation == 0.0) {
                        Node node1 = graph.getNodeByName(node1Name);
                        Node node2 = graph.getNodeByName(node2Name);
                        Node node3 = graph.getNodeByName(node3Name);

                        if (node1 != null && node2 != null && node3 != null) {
                            // Supprimer les liens entre les nœuds
                            node1.removeLink(node2);
                            node2.removeLink(node1);
                            node1.removeLink(node3);
                            node3.removeLink(node1);
                            node2.removeLink(node3);
                            node3.removeLink(node2);
                        }

                        // Optionnel : Supprimer les lignes de t_edges_2 où corr_part = 0
                        String deleteQuery = "DELETE FROM t_edges_2 WHERE node1 = ? AND node2 = ? AND node3 = ?";
                        try (var deleteStatement = connection.prepareStatement(deleteQuery)) {
                            deleteStatement.setString(1, node1Name);
                            deleteStatement.setString(2, node2Name);
                            deleteStatement.setString(3, node3Name);
                            deleteStatement.executeUpdate();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //NB! Il faut utiliser le chi squared test sur les champs initialement numériques
        // (donc pas les champs pour lesquels nous avons calculés le hash qui sont de la forme nomtable_num)
        // 8) Utilisation du test du Chi-carré et suppression des liens entre les triplets de nœuds
        try {
            String query = "SELECT node1, node2, node3, corr_part FROM t_edges_2";
            try (var statement = connection.createStatement();
                 var resultSet = statement.executeQuery(query)) {

                while (resultSet.next()) {
                    String node1Name = resultSet.getString("node1");
                    String node2Name = resultSet.getString("node2");
                    String node3Name = resultSet.getString("node3");
                    Double partialCorrelation = resultSet.getObject("corr_part", Double.class);

                    // Vérifier si la corrélation partielle est nulle ou égale à 0
                    if (partialCorrelation != null && partialCorrelation == 0.0) {
                        Node node1 = graph.getNodeByName(node1Name);
                        Node node2 = graph.getNodeByName(node2Name);
                        Node node3 = graph.getNodeByName(node3Name);

                        if (node1 != null && node2 != null && node3 != null) {
                            // Vérifier si les champs sont initialement numériques
                            if (node1.isNumeric() && node2.isNumeric() && node3.isNumeric()) {
                                // Utiliser le test du Chi-carré pour vérifier l'indépendance conditionnelle
                                boolean isIndependent = PCAlgorithmUtils.performChiSquaredTestForThreeVariables(connection, tableName, node1Name, node2Name, node3Name);

                                if (isIndependent) {
                                    // Supprimer les liens entre les nœuds
                                    node1.removeLink(node2);
                                    node2.removeLink(node1);
                                    node1.removeLink(node3);
                                    node3.removeLink(node1);
                                    node2.removeLink(node3);
                                    node3.removeLink(node2);

                                    // Optionnel : Supprimer les lignes de t_edges_2 où corr_part = 0
                                    String deleteQuery = "DELETE FROM t_edges_2 WHERE node1 = ? AND node2 = ? AND node3 = ?";
                                    try (var deleteStatement = connection.prepareStatement(deleteQuery)) {
                                        deleteStatement.setString(1, node1Name);
                                        deleteStatement.setString(2, node2Name);
                                        deleteStatement.setString(3, node3Name);
                                        deleteStatement.executeUpdate();
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }



    }


}

