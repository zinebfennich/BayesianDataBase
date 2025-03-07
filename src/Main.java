import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import database.DatabaseUtils;
import database.ColumUtils;
import structures.Graph;
import structures.Node;
import utils.Combiner;

public class Main {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/imdb";
        String user = "postgres";
        String password = "psqlpass";
        String tableName = "aka_name"; // Nom de la table à tester

        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement()) {


            //  Appliquer `alterTableAddNumFields` pour ajouter les colonnes `_num`
            DatabaseUtils.alterTableAddNumFields(connection, tableName);

            Graph graph = new Graph();
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





        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}