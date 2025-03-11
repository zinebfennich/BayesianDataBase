import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import algorithms.PCAlgorithm;
import algorithms.PCAlgorithm;
import database.DatabaseUtils;
import database.ColumUtils;

public class Main {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/imdb";
        String user = "postgres";
        String password = "";
        String tableName = "title_episode"; // Nom de la table à tester

        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement()) {


//            //  Appliquer `alterTableAddNumFields` pour ajouter les colonnes `_num`
//            DatabaseUtils.alterTableAddNumFields(connection, tableName);
            //PCAlgorithm.discoverCausalStructure(connection, tableName);

            PCAlgorithm pc = new PCAlgorithm();
            pc.discoverCausalStructure(connection);

        } catch (SQLException e) {
            e.printStackTrace();
        }








    }
}
