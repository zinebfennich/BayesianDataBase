import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import database.DatabaseUtils;
import database.ColumUtils;
import structures.Graph;
import structures.Node;
import utils.Combiner;

public class Main {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/imdb";
        String user = "postgres";
        String password = "psqlpass";
        String tableName = "aka_name"; // Nom de la table à tester



    }
}