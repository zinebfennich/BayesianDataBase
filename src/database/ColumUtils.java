package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Cette classe est dédiée à des opérations sur les colonnes des tables.
 * Elle va gérer des tâches comme la création, la modification, ou la
 * suppression
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
            Statement statement = connection.createStatement();// retourne une instance de statement
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
     * 
     * @param connection La connexion à la base de données
     * @param nomtable   Le nom de la table
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
     * 
     * @param connection La connexion à la base de données
     * @param nomtable   Le nom de la table
     * @param columnName Le nom de la colonne à ajouter
     * @param columnType Le type de la colonne à ajouter (par exemple INTEGER)
     * @throws SQLException Si une erreur SQL survient
     */
    public static void addColumn(Connection connection, String nomtable, String columnName, String columnType)
            throws SQLException {
        String alterStatement = "ALTER TABLE " + nomtable + " ADD COLUMN " + columnName + " " + columnType + ";";
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(alterStatement);
            System.out.println("Ajout de la colonne : " + columnName);
        }
    }

    /**
     * Renomme une colonne d'une table.
     * 
     * @param connection    La connexion à la base de données
     * @param nomtable      Le nom de la table
     * @param oldColumnName Le nom actuel de la colonne
     * @param newColumnName Le nouveau nom de la colonne
     * @throws SQLException Si une erreur SQL survient
     */
    public static void renameColumn(Connection connection, String nomtable, String oldColumnName, String newColumnName)
            throws SQLException {
        String renameStatement = "ALTER TABLE " + nomtable + " RENAME COLUMN " + oldColumnName + " TO " + newColumnName
                + ";";
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(renameStatement);
            System.out.println("Renommage de la colonne : " + oldColumnName + " en " + newColumnName);
        }
    }

    /**
     * Supprime une colonne d'une table spécifiée.
     * 
     * @param connection La connexion à la base de données
     * @param nomtable   Le nom de la table
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

    /**
     * retourne true si la colonne est initialement numérique, false sinon.
     * 
     * @param connection
     * @param nomtable
     * @param columnName
     * @return
     * @throws SQLException
     */
    public static boolean columnIsNumeric(Connection connection, String nomtable, String columnName)
            throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        // Vérifier si une colonne _num existe
        String numericColumnName = columnName + "_num";
        try (ResultSet columns = metaData.getColumns(null, null, nomtable, numericColumnName)) {
            if (columns.next()) {
                return false; // donc la colonne originale est textuelle
            }
        }
        return true; // Aucune colonne _num n'existe donc c'est numérique
    }

    /**
     * retourne toutes les variables possibles d'une table à part une paire une
     * paire de variables.
     * 
     * @param connection
     * @param tableName
     * @param node1
     * @param node2
     * @return
     * @throws SQLException
     */
    public static List<String> getColumnsExceptTextAndPair(Connection connection, String tableName, String node1,
            String node2) throws SQLException {
        List<String> columns = new ArrayList<>();
        try (Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(
                        "SELECT column_name FROM information_schema.columns WHERE table_name = '" + tableName + "'")) {

            while (resultSet.next()) {
                String columnName = resultSet.getString("column_name");
                if (!columnName.equals(node1) && !columnName.equals(node2)
                        && columnIsNumeric(connection, tableName, columnName)) {
                    columns.add(columnName);
                }
            }
        }
        return columns;
    }

    /**
     * retourne toutes les variables possibles d'une table à part une paire une
     * paire de variables.
     *
     * @param connection
     * @param tableName
     * @param node1
     * @param node2
     * @return
     * @throws SQLException
     */
    public static List<String> getColumnsExceptTextAndPair(Connection connection, String tableName, String node1,
                                                           String node2) throws SQLException {
        List<String> columns = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "SELECT column_name FROM information_schema.columns WHERE table_name = '" + tableName + "'")) {
            while (resultSet.next()) {
                String columnName = resultSet.getString("column_name");
                if (!columnName.equals(node1) && !columnName.equals(node2)
                        && columnIsNumeric(connection, tableName, columnName)) {
                    columns.add(columnName);
                }
            }
        }
        return columns;
    }
    /**
     * Crée une nouvelle colonne binned (plages) pour une variable numérique.
     *
     * @param connection Connexion à la base de données
     * @param tableName  Nom de la table
     * @param columnName Nom de la colonne à discrétiser
     * @return Nom de la colonne transformée (`column_binned`)
     * @throws SQLException Si une erreur SQL survient
     */
    public static String createBinnedColumn(Connection connection, String tableName, String columnName)
            throws SQLException {
        String binnedColumnName = columnName + "_binned";
        // Vérifier si la colonne existe déjà pour éviter de la recréer
        if (!columnExists(connection, tableName, binnedColumnName)) {
            addColumn(connection, tableName, binnedColumnName, "TEXT");

            // Déterminer les seuils de binning automatiquement (quartiles)
            List<Double> thresholds = calculateBinningThresholds(connection, tableName, columnName);

            // Appliquer les seuils pour catégoriser les valeurs
            String updateSQL = "UPDATE " + tableName + " SET " + binnedColumnName + " = " +
                    "CASE " +
                    "WHEN " + columnName + " < ? THEN 'Faible' " +
                    "WHEN " + columnName + " BETWEEN ? AND ? THEN 'Moyen' " +
                    "ELSE 'Élevé' END";

            try (PreparedStatement statement = connection.prepareStatement(updateSQL)) {
                statement.setDouble(1, thresholds.get(0)); // 1er quartile
                statement.setDouble(2, thresholds.get(0)); // 1er quartile
                statement.setDouble(3, thresholds.get(1)); // 3e quartile
                statement.executeUpdate();
            }
        }

        return binnedColumnName;
    }

    /**
     * Calcule les seuils pour faire du binning sur une colonne numérique.
     * Utilise les quartiles (Q1 et Q3) pour discrétiser en 3 groupes.
     *
     * @param connection Connexion à la base de données
     * @param tableName  Nom de la table
     * @param columnName Nom de la colonne à analyser
     * @return Liste contenant [Q1, Q3] comme seuils
     * @throws SQLException Si une erreur SQL survient
     */
    private static List<Double> calculateBinningThresholds(Connection connection, String tableName, String columnName)
            throws SQLException {
        String sql = "SELECT percentile_cont(0.25) WITHIN GROUP (ORDER BY " + columnName + ") AS Q1, " +
                "percentile_cont(0.75) WITHIN GROUP (ORDER BY " + columnName + ") AS Q3 " +
                "FROM " + tableName;

        List<Double> thresholds = new ArrayList<>();
        try (Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)) {
            if (resultSet.next()) {
                thresholds.add(resultSet.getDouble("Q1"));
                thresholds.add(resultSet.getDouble("Q3"));
            }
        }
        return thresholds;
    }

}
