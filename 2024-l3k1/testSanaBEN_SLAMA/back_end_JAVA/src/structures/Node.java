package structures;

import java.util.ArrayList;
import java.util.List;

public class Node {
    private String name; //nom de la colonne/variable
    private List<Node> descendants; // Liste des descendants (noeuds vers lesquels il y a un lien sortant)

    public Node(String name) {
        this.name = name;
        this.descendants = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Ajoute un lien unidirectionnel du noeud actuel vers un descendant (A → B).
     * @param node2 Le noeud descendant.
     */
    public void addLink(Node node2) {
        // noeud1 -> noeud2
        if (!descendants.contains(node2)) {
            this.descendants.add(node2);
        }
    }

    /**
     * Retire un lien unidirectionnel du noeud actuel vers un descendant (A → B).
     * @param node2 Le noeud descendant dont le lien doit être retiré.
     */
    public void removeLink(Node node2) {
        //suppression du lien noeud1 -> noeud2
        descendants.remove(node2);
    }

    public List<Node> getDescendants() {
        return descendants;
    }

    public boolean isNumeric() {
        return !name.endsWith("_num");
    }


}


