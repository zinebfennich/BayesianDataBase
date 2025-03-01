package algorithms;

import database.DatabaseUtils;
import structures.*;
import utils.Combiner;

import java.sql.Connection;
import java.sql.SQLException;


/**
 * Contient l'implémentation de l'algorithme PC pour la découverte causale.
 */
public class PCAlgorithm {
    private String tableName; //à revoir
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




    }


}

