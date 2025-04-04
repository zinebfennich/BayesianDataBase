package database;

import java.sql.*;

/**
 * Cette classe contient des méthodes qui manipulent des structures globales de la base de données, telles que des tables,
 * des colonnes au niveau global, ou des procédures stockées. Elle est centrée sur la gestion des tables, des requêtes SQL
 * générales, ou des opérations globales sur la base de données.
 * Cette classe regroupe des opérations qui agissent au niveau de la structure de la base de données dans son ensemble.
 */
public class DatabaseUtils {
    //NB : penser à sécuriser les méthodes qui ont accès/modifient la bdd



    /**
     * Modifie la table spécifiée pour ajouter des colonnes numériques (_num)
     * basées sur les colonnes alphanumériques existantes et les remplir avec hashtext().
     *
     * @param connection Connexion à la base de données
     * @param nomtable Nom de la table à modifier
     * @throws SQLException Si une erreur SQL survient
     */
    public static void alterTableAddNumFields(Connection connection, String nomtable) throws SQLException {
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
}
