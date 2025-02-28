package structures;

import java.util.ArrayList;
import java.util.List;

public class Node {
    private String name;  // Nom de la colonne (variable)
    private List<Node> neighbors;

    public Node(String name) {
        this.name = name;
        this.neighbors = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void addlink(Node node2) {
        this.neighbors.add(node2);
    }

    public void removelink(Node node2) {
        this.neighbors.add(node2);
    }

    public List<Node> getNeighbors() {
        return neighbors;
    }
}

