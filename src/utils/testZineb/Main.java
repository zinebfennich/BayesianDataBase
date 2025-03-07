import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import database.DatabaseUtils;
import structures.Graph;
import structures.Node;
import utils.Combiner;
import utils.PCAlgorithmUtils;

public class Main {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/imdb";
        String user = "postgres";
        String password = "psqlpass";
        String tableName = "title_akas"; // Nom de la table à tester

        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement()) {

            // Appliquer `alterTableAddNumFields` pour ajouter les colonnes `_num`
            DatabaseUtils.alterTableAddNumFields(connection, tableName);

            //  Initialiser le graphe et ajouter les colonnes comme noeuds
            Graph graph = new Graph();
            try {
                graph.addTableNodesToGraph(connection, tableName);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            graph.createCompleteGraph();

            //  Générer toutes les **paires de colonnes** et calculer leurs corrélations
            Node[] nodesTab = new Node[graph.getNodes().size()];
            graph.getNodes().toArray(nodesTab);
            Combiner<Node> combinerPairs = new Combiner<>(2, nodesTab);

            Node[] paire = new Node[2];
            while (combinerPairs.searchNext(paire)) {
                String colonne1 = paire[0].getName();
                String colonne2 = paire[1].getName();

                // Calcul de la corrélation entre les deux colonnes
                try {
                    DatabaseUtils.testcall1(connection, tableName, colonne1, colonne2);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            // Générer toutes les combinaisons de trois colonnes et tester la corrélation partielle
            Combiner<Node> combinerTriplets = new Combiner<>(3, nodesTab);
            Node[] triplet = new Node[3];

            while (combinerTriplets.searchNext(triplet)) {
                String colonne1 = triplet[0].getName();
                String colonne2 = triplet[1].getName();
                String colonne3 = triplet[2].getName();

                // Calculer la corrélation partielle pour ces trois variables
                try {
                    DatabaseUtils.testcall2(connection, tableName, colonne1, colonne2, colonne3);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                // **Ajoute la condition ici** pour ignorer `id`
                if (!colonne1.equals("id") && !colonne2.equals("id") && !colonne3.equals("id")) {
                    boolean isIndependent = false;
                    try {
                        isIndependent = PCAlgorithmUtils.performChiSquaredTestForThreeVariables(
                                connection, tableName, colonne1, colonne2, colonne3);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    // Affichage des résultats
                    if (isIndependent) {
                        System.out.println("Les variables " + colonne1 + ", " + colonne2 + " et " + colonne3 + " sont **indépendantes conditionnellement**.");
                    } else {
                        System.out.println("Les variables " + colonne1 + ", " + colonne2 + " et " + colonne3 + " sont **corrélées**.");
                    }
                }
            }



        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
