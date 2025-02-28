package databaseTests;
import database.*;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.sql.*;

public class DatabaseUtilsTest {

    private Connection connection;
    private String tableName ="test_table" ;

    @BeforeEach
    public void setUp() throws SQLException {
        // Se connecter à une base de données en mémoire H2 pour les tests
        connection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "");

        // Créer une table pour les tests
        Statement stmt = connection.createStatement();
        stmt.execute("CREATE TABLE tableName (id INT PRIMARY KEY, name VARCHAR(255))");
    }


    @AfterEach
    public void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }


    @Test
    public void testAlterTableAddNumFields() throws SQLException {
        DatabaseUtils.alterTableAddNumFields(connection, tableName);

        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet rs = metaData.getColumns(null, null, tableName, null);

        int columnCount = 0; //nb de colonnes
        while (rs.next()) {
            columnCount++;
        }

        assertEquals();
    }


}

