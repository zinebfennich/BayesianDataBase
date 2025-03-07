package structures;

import database.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;



public class Graph {
    //NB: si on peut avoir un réseau bayésien avec des variables de plusieurs tables envisager de changer peut-etre
    //la liste en map <Node, table>
    private List<Node> nodes;

    public Graph() {
        nodes = new ArrayList<Node>();
    }

    public void addNode(Node node) {
        nodes.add(node);
    }

    public List<Node> getNodes() {
        return nodes;
    }

    /**
     * Ajoute les noms des colonnes de type INTEGER d'une table(les variables) à l'objet Graph sous forme de Nodes.
     * @param connection Connexion à la base de données
     * @param tableName  Nom de la table
     * @throws SQLException
     */
    public void addTableNodesToGraph(Connection connection, String tableName) throws SQLException {
        String[] columnNames = ColumnUtils.getColumnNames1(connection, tableName);
        if (columnNames != null) {
            for (String columnName : columnNames) {
                Node node = new Node(columnName);
                addNode(node);
            }
        }
    }

    /**
     * pour chaque noeud dans nodes, on crée un lien entre ce noeud et tous les autres noeuds
     */
    public void createCompleteGraph() {
        // Parcours de tous les noeuds
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = 0; j < nodes.size(); j++) {
                if (i != j) { //pas de boucles
                    nodes.get(i).addLink(nodes.get(j));
                }
            }
        }
    }
}
