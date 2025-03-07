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
        //INDEPENDANCES CONDITIONNELLES D ORDRE 1 (A et B)
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

                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 4) Calcul des corrélations partielles pour les triplets de noeuds
        //INDEPENDANCE CONDITIONNELLE D'ORDRE 1 (A et B | C)
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
        // NB! Il faut utiliser le chi squared test sur les champs initialement numériques
        // (donc pas les champs pour lesquels nous avons calculé le hash qui sont de la forme nomtable_num)

        try {
            String query = "SELECT node1, node2, node3, corr_part, correlation_exists FROM t_edges_2";
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
                                System.out.println("Suppression du lien entre " + node1Name + " et " + node2Name + " car corr = 0 sachant " + node3Name);
                                if (node1 != null && node2 != null && node3 != null) {
                                    node1.removeLink(node2);
                                    node2.removeLink(node1);
                                }
                            }

                            // Mise à jour de la valeur de "correlation_exists" dans la base de données
                            String updateQuery = "UPDATE t_edges_2 SET correlation_exists = ? WHERE node1 = ? AND node2 = ? AND node3 = ?";
                            try (var updateStatement = connection.prepareStatement(updateQuery)) {
                                boolean correlationExists = partialCorrelation != null && Math.abs(partialCorrelation) >= 0.0001;  // Si corr_part ≈ 0, mettre FALSE
                                updateStatement.setBoolean(1, correlationExists);
                                updateStatement.setString(2, node1Name);
                                updateStatement.setString(3, node2Name);
                                updateStatement.setString(4, node3Name);

                                int rowsUpdated = updateStatement.executeUpdate();
                                System.out.println("Mise à jour de correlation_exists (" + correlationExists + ") pour : "
                                        + node1Name + ", " + node2Name + ", " + node3Name);
                                System.out.println("✔ Nombre de lignes mises à jour : " + rowsUpdated);

                                if (rowsUpdated == 0) {
                                    System.out.println("Aucune ligne mise à jour ! Vérifie si ces valeurs existent bien dans t_edges_2.");
                                }
                            } catch (SQLException e) {
                                throw new RuntimeException("Erreur lors de la mise à jour de correlation_exists.", e);
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException("Erreur lors de la récupération des données dans t_edges_2.", e);
            }

            // 6) Calcul des corrélations partielles pour les quadruplets de noeuds
            //INDEPENDANCE CONDITIONNELLE D'ORDRE 2 (A et B | {C,D})
            Combiner<Node> combinerQuadruplets = new Combiner<>(4, nodesTab);
            Node[] quadruplet = new Node[4];

            while (combinerQuadruplets.searchNext(quadruplet)) {
                Node node1 = quadruplet[0];
                Node node2 = quadruplet[1];
                Node node3 = quadruplet[2];
                Node node4 = quadruplet[3];

                String colonne1 = node1.getName();
                String colonne2 = node2.getName();
                String colonne3 = node3.getName();
                String colonne4 = node4.getName();

                try {
                    // Appel de testcall3 pour stocker la corrélation partielle dans t_edges_ci1
                    DatabaseUtils.testcall4(connection, tableName, colonne1, colonne2, colonne3, colonne4);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            // 7) Suppression des liens entre les quadruplets de noeuds qui ont corrélation partielle = 0 dans t_edges_ci1
            try {
                String query1 = "SELECT node1, node2, node3, node4, corr_part, correlation_exists FROM t_edges_ci1";
                try (var statement = connection.createStatement();
                     var resultSet = statement.executeQuery(query1)) {

                    while (resultSet.next()) {
                        String node1Name = resultSet.getString("node1");
                        String node2Name = resultSet.getString("node2");
                        String node3Name = resultSet.getString("node3");
                        String node4Name = resultSet.getString("node4");
                        Boolean correlationExists = resultSet.getBoolean("correlation_exists");

                        // Si la corrélation partielle est proche de 0 et que l'indépendance conditionnelle est vérifiée avec chi-carré
                        if (correlationExists != null && !correlationExists) {
                            Node node1 = graph.getNodeByName(node1Name);
                            Node node2 = graph.getNodeByName(node2Name);
                            Node node3 = graph.getNodeByName(node3Name);
                            Node node4 = graph.getNodeByName(node4Name);

                            // Vérifier si les variables sont numériques avant d'appliquer le test du Chi-carré
                            if (node1 != null && node2 != null && node3 != null && node4 != null) {
                                boolean isIndependent = PCAlgorithmUtils.performChiSquaredTestForFourVariables(connection, tableName,
                                        node1Name, node2Name, node3Name, node4Name);
                                if (isIndependent) {
                                    System.out.println("Suppression du lien entre " + node1Name + ", " + node2Name + ", "
                                            + node3Name + " et " + node4Name + " car corr = 0 sachant " + node3Name + " et " + node4Name);
                                    node1.removeLink(node2);
                                    node2.removeLink(node1);
                                }
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de l'exécution de l'algorithme PC.", e);
        }

        //8) découvrir les v_structures
        // 2 variables A et B ont des liens vers une 3ème variable mais ne sont pas directement liées entre elles
        VStructureDetector detector = new VStructureDetector();
        detector.detectVStructures(graph);

        // Une fois les v-structures détectées, il faut orienter les arêtes restantes pour éviter des v-structures nouvelles
        detector.orientRemainingEdges(graph);



    }
}
