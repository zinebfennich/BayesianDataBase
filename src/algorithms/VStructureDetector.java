package algorithms;

import structures.Graph;
import structures.Node;

import java.util.List;

public class VStructureDetector {

    // Détecte les v-structures dans le graphe
    public void detectVStructures(Graph graph) {
        List<Node> nodes = graph.getNodes();

        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {

                Node A = nodes.get(i);
                Node B = nodes.get(j);

                // Vérifier si A et B ne sont pas directement liés
                if (!A.getDescendants().contains(B) && !B.getDescendants().contains(A)) {

                    // Vérifier s'il existe un nœud C qui est un parent de A et de B
                    for (Node C : nodes) {
                        if (C != A && C != B) {
                            boolean A_corr_C = C.getDescendants().contains(A);  // Vérifie si C → A
                            boolean B_corr_C = C.getDescendants().contains(B);  // Vérifie si C → B

                            // Si C conditionne à la fois A et B, c'est une v-structure
                            if (A_corr_C && B_corr_C) {
                                System.out.println("V-structure détectée entre " + A.getName() + ", " + B.getName() + " et " + C.getName());
                                //il faut insérer le triplet trouvé dans vstructures
                                //il faut orienter les fleches du v structure dans le graphe
                            }
                        }
                    }
                }
            }
        }
    }

    //a revoir / rectifier
    public static void orientRemainingEdges(Graph graph) {
        List<Node> nodes = graph.getNodes();

        // Pour chaque paire de nœuds (A, B), si elle n'est pas déjà orientée, on l'oriente
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                Node A = nodes.get(i);
                Node B = nodes.get(j);

                // Vérifie si A et B sont déjà liés
                if (!A.getDescendants().contains(B) && !B.getDescendants().contains(A)) {
                    // Oriente l'arête de manière à ne pas créer de nouvelles v-structures
                    // Par exemple, on oriente de A vers B
                    A.addLink(B);
                    System.out.println("Arête orientée de " + A.getName() + " vers " + B.getName());
                }
            }
        }
    }


}

