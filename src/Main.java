import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import algorithms.PCAlgorithm;
import database.ColumUtils;
import database.DatabaseUtils;

public class Main {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/imdb";
        String user = "postgres";
        String password = "psqlpass";
        String tableName = "aka_name"; // Nom de la table à tester
        // Établir la connexion à la base de données
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            PCAlgorithm pcAlgorithm = new PCAlgorithm(tableName);
            DatabaseUtils.alterTableAddNumFields(connection, tableName);
            String[] columns = ColumUtils.getColumnNames1(connection, tableName);
            pcAlgorithm.discoverCausalStructure(connection, columns);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}