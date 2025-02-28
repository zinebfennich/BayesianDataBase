package database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
}
