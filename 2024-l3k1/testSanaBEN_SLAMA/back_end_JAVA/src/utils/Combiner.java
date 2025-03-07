package utils;

/**
 * Classe utilitaire pour générer des combinaisons d'éléments d'un tableau.
 *
 * @param <T> Type des éléments à combiner
 */
public class Combiner<T> {

    protected int count; // le nombre d'éléments qu'on veut dans chaque combinaison (2,3..)
    protected T[] array; //  le tableau d'éléments à combiner (ici on utiliser les noms de colonnes
    protected int[] indexes;

    public Combiner(int count, T[] array) {
        super();
        this.count = count;
        this.array = array;
        indexes = new int[count];
        for (int i = 0; i < count; i++)
            indexes[i] = i;
    }

    public boolean searchNext(T[] result) {
        if (indexes == null)
            return false;

        int resultIndex = 0;
        for (int index : indexes)
            result[resultIndex++] = array[index];

        int indexesRank = count - 1;
        int arrayRank = array.length - 1;
        while (indexes[indexesRank] == arrayRank) {
            if (indexesRank == 0) {
                indexes = null;
                return true;
            }
            indexesRank--;
            arrayRank--;
        }

        int restartIndex = indexes[indexesRank] + 1;
        while (indexesRank < count)
            indexes[indexesRank++] = restartIndex++;

        return true;
    }
}

