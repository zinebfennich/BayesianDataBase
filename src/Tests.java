
import java.sql.*;
import java.util.Arrays;

/**
 * Classe principale pour tester des opérations sur une base de données
 * PostgreSQL.
 * Permet de récupérer des noms de colonnes et d'insérer des données dans
 * différentes tables.
 */
public class Tests {

    /**
     * Point d'entrée principal du programme.
     * Établit une connexion à la base de données, récupère les noms de colonnes
     * et exécute des opérations d'insertion dans différentes tables.
     *
     * @param args Arguments de la ligne de commande (non utilisés)
     */
    public static void main(String[] args) {

        //String tableName = "title_principals";
        String jdbcUrl1 = "jdbc:postgresql://localhost:5432/imdb";
        String username1 = "postgres";
        String password1 = "psqlpass";

        int i = 0;

        try {
            Connection connection = DriverManager.getConnection(jdbcUrl1, username1, password1);
//            String[] tokens = getColumnNames1(connection,"title_crew" );//retourne les noms des colonnes numériques sous forme de tableau de chaînes
//            //System.out.println(tokens[1]);
//            //System.out.println("Starting ....\n");
//            int count = 3;
//            Combiner<String> combiner = new Combiner<String>(count, tokens);
//            String[] result = new String[count];
//            //System.out.println("Starting 2 ....\n");
//            //System.out.println("Result ...." + result[0]);

            //test pour hasher les colonnes _num
            alterTableAddNumFields(connection,"films");

            //test pour générer les combinaisons de colonnes et les passer à testCall1 qui les insere dans t_edges et calcule les corrélations
            // Supposons que vous avez déjà une méthode pour récupérer les colonnes depuis la base de données.
            String[] colonnes = getColumnNames1(connection, "films");

            // Créez une instance de Combiner pour générer toutes les paires de colonnes (2 éléments par combinaison)
            Combiner<String> combiner = new Combiner<>(2, colonnes);

            // Tableau pour stocker la combinaison actuelle (chaque paire de nœuds)
            String[] paire = new String[2];

            // Tant qu'il y a une nouvelle combinaison (paires de colonnes)
            while (combiner.searchNext(paire)) {
                String colonne1 = paire[0];  // Premier élément de la paire (colonne 1)
                String colonne2 = paire[1];  // Deuxième élément de la paire (colonne 2)

                // Nom de la table dans laquelle vous insérez les données
                String tableName = "films";  // Vous pouvez adapter le nom de la table ici selon vos besoins.

                // Appeler la méthode testcall1 pour insérer la paire de colonnes et calculer la corrélation
                try {
                    testcall1(connection, tableName, colonne1, colonne2);
                } catch (SQLException e) {
                    e.printStackTrace();  // Gérer les erreurs SQL
                }
            }




//            while (combiner.searchNext(result)) {
//                System.out.println("Starting 3 ....\n");
//                i = i + 1;
//                System.out.println("i...." + i);
//                System.out.println(Arrays.toString(result));
//                String column1 = result[0];
//                String column2 = result[1];
//                System.out.println("Inserting ....\n");
//                System.out.println(result[0]);
//                System.out.println(result[1]);
//                testcall1(connection, tableName, column1, column2);
//                System.out.println("Calculating ....\n");
//                System.out.println(result[0]);
//                System.out.println(result[1]);
//                // count=3;
//                String column3 = result[2];
//                System.out.println(result[2]);
//                testcall3(connection, tableName, column1, column2, column3);
//
//            }

            // count = 3;
            // String[] result1 = new String[count];
            // while (combiner.searchNext(result1))
            // {
            // i=i+1;
            // System.out.println("i...."+i);
            // System.out.println(Arrays.toString(result1));
            // String column1 = result1[0];
            // String column2 = result1[1];
            // String column3 = result1[2];
            // System.out.println("Inserting ....\n");
            // System.out.println(result1[0]);
            // System.out.println(result1[1]);

            // System.out.println("Calculating ....\n");
            // System.out.println(result[0]);
            // System.out.println(result[1]);

            // }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Récupère les noms des colonnes de type INTEGER d'une table donnée.
     *
     * @param connection Connexion à la base de données
     * @param tableName  Nom de la table à interroger
     * @return Tableau des noms de colonnes
     * @throws SQLException Si une erreur SQL survient
     */
    public static String[] getColumnNames1(Connection connection, String tableName) throws SQLException {

        String[] columnNames = null;
        try {
            Statement statement = connection.createStatement();//retourne une instance de statement
            Statement statementCount = connection.createStatement();
            System.out.println("Get column names ....\n");
            // Adjust SQL for PostgreSQL
            String sql = "SELECT column_name FROM information_schema.columns WHERE table_name = '" + tableName
                    + "' AND data_type = 'integer'";
            String sqlcount = "SELECT count(*) FROM information_schema.columns WHERE table_name = '" + tableName
                    + "' AND data_type = 'integer'";

            ResultSet resultSet = statement.executeQuery(sql);
            ResultSet resultSetCount = statementCount.executeQuery(sqlcount);

            int columnCount = 0;
            if (resultSetCount.next()) {
                columnCount = resultSetCount.getInt(1);
            }

            columnNames = new String[columnCount];
            System.out.println(columnCount);

            System.out.println("Get column names 1....\n");
            int i = 0;
            while (resultSet.next()) {
                columnNames[i] = resultSet.getString("column_name");
                System.out.println(columnNames[i]);
                i++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error\n");
        }
        return columnNames;
    }

    /**
     * Classe utilitaire pour générer des combinaisons d'éléments d'un tableau.
     *
     * @param <T> Type des éléments à combiner
     */
    public static class Combiner<T> {

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

    /**
     * La méthode testcall1 fait deux choses :
     * Insertion des 2 variables dans la table T_edges.
     * Appel de la procédure stockée p_corr_postgres pour calculer les corrélations.
     *
     * @param connection Connexion à la base de données
     * @param tableName  Nom de la table source
     * @param column1    Nom de la première colonne
     * @param column2    Nom de la deuxième colonne
     * @throws SQLException Si une erreur SQL survient
     */
    public static void testcall1(Connection connection, String tableName, String column1, String column2)
            throws SQLException {

        // 1. Insérer les données dans la table T_edges
        String sqlInsert = "INSERT INTO T_edges (sname, node1, node2, corr) VALUES (?, ?, ?, ?)";
        try (PreparedStatement insertStatement = connection.prepareStatement(sqlInsert)) {
            insertStatement.setString(1, tableName);
            insertStatement.setString(2, column1);
            insertStatement.setString(3, column2);
            insertStatement.setInt(4, 0); // initialisation à 0, mais va être mis à jour par la procédure
            insertStatement.executeUpdate();
        }

        // 2. Appeler la procédure p_corr_postgres pour calculer la corrélation
        String sqlCall = "CALL p_corr_postgres(?, ?, ?)";
        try (CallableStatement callStatement = connection.prepareCall(sqlCall)) {
            // Passer les paramètres à la procédure
            callStatement.setString(1, tableName); // p_supernode
            callStatement.setString(2, column1);   // p_subnode1
            callStatement.setString(3, column2);   // p_subnode2

            // Exécuter la procédure
            callStatement.execute();
        }
    }

    /**
     * Insère des données dans la table T_edges_2.
     *
     * @param connection Connexion à la base de données
     * @param tableName  Nom de la table source
     * @param column1    Nom de la première colonne
     * @param column2    Nom de la deuxième colonne
     * @param column3    Nom de la troisième colonne
     * @throws SQLException Si une erreur SQL survient
     */
    public static void testcall2(Connection connection, String tableName, String column1, String column2,
                                 String column3) throws SQLException {

        String sql = "INSERT INTO T_edges_2 (sname, node1, node2, node3,corr_part) VALUES (?, ?, ?, ?,?)";
        CallableStatement statement = connection.prepareCall(sql);
        statement.setString(1, tableName);
        statement.setString(2, column1);
        statement.setString(3, column2);
        statement.setString(4, column3);
        statement.setInt(5, 0);
        statement.executeUpdate();
    }

    /**
     * Insère des données dans la table T_edges_ci1.
     *
     * @param connection Connexion à la base de données
     * @param tableName  Nom de la table source
     * @param column1    Nom de la première colonne
     * @param column2    Nom de la deuxième colonne
     * @param column3    Nom de la troisième colonne
     * @throws SQLException Si une erreur SQL survient
     */
    public static void testcall3(Connection connection, String tableName, String column1, String column2,
                                 String column3) throws SQLException {

        String sql = "INSERT INTO T_edges_ci1 (sname, node1, node2, node3,corr12,corr13,corr23) VALUES (?, ?, ?, ?,?,?,?)";
        CallableStatement statement = connection.prepareCall(sql);
        statement.setString(1, tableName);
        statement.setString(2, column1);
        statement.setString(3, column2);
        statement.setString(4, column3);
        statement.setInt(5, 0);
        statement.setInt(6, 0);
        statement.setInt(7, 0);
        statement.executeUpdate();
    }

    /**
     * Modifie la table spécifiée pour ajouter des colonnes numériques (_num)
     * basées sur les colonnes alphanumériques existantes et les remplir avec hashtext().
     *
     * @param connection Connexion à la base de données
     * @param nomtable Nom de la table à modifier
     * @throws SQLException Si une erreur SQL survient
     */
    private static void alterTableAddNumFields(Connection connection, String nomtable) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            DatabaseMetaData metadata = connection.getMetaData();
            ResultSet columns = metadata.getColumns(null, null, nomtable, null);

            // Vérifier les colonnes existantes et ajouter les colonnes _num si nécessaire
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                String dataType = columns.getString("TYPE_NAME");

                // Vérifier si la colonne est alphanumérique
                if (dataType.matches("(?i)char.*|varchar.*|text")) {
                    String columnNum = columnName + "_num";

                    // Vérifier si la colonne _num existe déjà
                    ResultSet numColumnCheck = metadata.getColumns(null, null, nomtable, columnNum);
                    if (!numColumnCheck.next()) {
                        // Ajouter la colonne _num si elle n'existe pas
                        String alterStatement = "ALTER TABLE " + nomtable + " ADD COLUMN " + columnNum + " INTEGER;";
                        statement.executeUpdate(alterStatement);
                        System.out.println("Ajout de la colonne : " + columnNum);
                    }
                    numColumnCheck.close();
                }
            }
            columns.close();

            // Script PL/pgSQL pour mettre à jour les colonnes _num avec hashtext()
            String hashUpdateScript = "DO $$ \n" +
                    "DECLARE \n" +
                    "    column_record RECORD;\n" +
                    "BEGIN\n" +
                    "    FOR column_record IN \n" +
                    "        SELECT column_name \n" +
                    "        FROM information_schema.columns \n" +
                    "        WHERE table_name = '" + nomtable + "'\n" +
                    "        AND column_name NOT LIKE '%_num'\n" +
                    "        AND data_type LIKE 'text%'\n" +
                    "    LOOP\n" +
                    "        EXECUTE 'UPDATE " + nomtable + " SET ' || quote_ident(column_record.column_name) || '_num = hashtext(' || quote_ident(column_record.column_name) || ');';\n" +
                    "    END LOOP;\n" +
                    "END $$;";

            // Exécuter la mise à jour des valeurs hashées
            statement.execute(hashUpdateScript);
            System.out.println("Mise à jour des valeurs hashées terminée.");
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }


}