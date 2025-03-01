package database;

import java.sql.*;

/**
 * Cette classe est dédiée à des opérations sur les colonnes des tables.
 * Elle va gérer des tâches comme la création, la modification, ou la suppression
 * de colonnes dans une table spécifique.
 *
 * Les opérations snt centrées sur des manipulations de colonnes à
 * l'intérieur de tables, telles que renommer une colonne, modifier
 * le type d'une colonne, ou vérifier l'existence d'une colonne.
 */
public class ColumUtils {

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
     * Vérifie si une colonne existe dans la table spécifiée.
     * @param connection La connexion à la base de données
     * @param nomtable Le nom de la table
     * @param columnName Le nom de la colonne à vérifier
     * @return true si la colonne existe, false sinon
     * @throws SQLException Si une erreur SQL survient
     */
    public static boolean columnExists(Connection connection, String nomtable, String columnName) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        try (ResultSet columnCheck = metadata.getColumns(null, null, nomtable, columnName)) {
            return columnCheck.next();
        }
    }

    /**
     * Ajoute une colonne à une table spécifiée.
     * @param connection La connexion à la base de données
     * @param nomtable Le nom de la table
     * @param columnName Le nom de la colonne à ajouter
     * @param columnType Le type de la colonne à ajouter (par exemple INTEGER)
     * @throws SQLException Si une erreur SQL survient
     */
    public static void addColumn(Connection connection, String nomtable, String columnName, String columnType) throws SQLException {
        String alterStatement = "ALTER TABLE " + nomtable + " ADD COLUMN " + columnName + " " + columnType + ";";
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(alterStatement);
            System.out.println("Ajout de la colonne : " + columnName);
        }
    }

    /**
     * Renomme une colonne d'une table.
     * @param connection La connexion à la base de données
     * @param nomtable Le nom de la table
     * @param oldColumnName Le nom actuel de la colonne
     * @param newColumnName Le nouveau nom de la colonne
     * @throws SQLException Si une erreur SQL survient
     */
    public static void renameColumn(Connection connection, String nomtable, String oldColumnName, String newColumnName) throws SQLException {
        String renameStatement = "ALTER TABLE " + nomtable + " RENAME COLUMN " + oldColumnName + " TO " + newColumnName + ";";
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(renameStatement);
            System.out.println("Renommage de la colonne : " + oldColumnName + " en " + newColumnName);
        }
    }

    /**
     * Supprime une colonne d'une table spécifiée.
     * @param connection La connexion à la base de données
     * @param nomtable Le nom de la table
     * @param columnName Le nom de la colonne à supprimer
     * @throws SQLException Si une erreur SQL survient
     */
    public static void dropColumn(Connection connection, String nomtable, String columnName) throws SQLException {
        String dropStatement = "ALTER TABLE " + nomtable + " DROP COLUMN " + columnName + ";";
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(dropStatement);
            System.out.println("Suppression de la colonne : " + columnName);
        }
    }

    public static void addNumColumnsIfNeeded(Connection connection, String nomtable) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        try (Statement statement = connection.createStatement();
             ResultSet columns = metadata.getColumns(null, null, nomtable, null)) {

            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                String dataType = columns.getString("TYPE_NAME");

                // Vérifier si la colonne est alphanumérique
                if (dataType.matches("(?i)char.*|varchar.*|text")) {
                    String columnNum = columnName + "_num";

                    if (!columnExists(connection, nomtable, columnNum)) {
                        // Ajouter la colonne _num si elle n'existe pas
                        addColumn(connection, nomtable, columnNum, "INTEGER");
                    }
                }
            }
        }
    }




}
