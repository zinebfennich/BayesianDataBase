import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import database.DatabaseUtils;
import database.ColumUtils;

public class Main {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/imdb";
        String user = "postgres";
        String password = "psqlpass";
        String tableName = "title_akas"; // Nom de la table à tester

        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement()) {


            //  Appliquer `alterTableAddNumFields` pour ajouter les colonnes `_num`
            DatabaseUtils.alterTableAddNumFields(connection, tableName);



        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
