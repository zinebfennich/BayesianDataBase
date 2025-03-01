package databaseTests;
import static org.junit.jupiter.api.Assertions.*;

import database.DatabaseUtils;
import org.junit.jupiter.api.*;
import java.sql.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DatabaseUtilsTest {

    private Connection connection;
    private final String tableName = "test_table";


    @BeforeAll
    void setupDatabase() throws SQLException {
        connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/imdb", "postgres", "psqlpass");
        //créer une table pour les tests
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DROP TABLE IF EXISTS " + tableName);
            statement.executeUpdate("CREATE TABLE " + tableName + " (id SERIAL PRIMARY KEY, name VARCHAR(255), description TEXT)");
            statement.executeUpdate("INSERT INTO " + tableName + " (name, description) VALUES ('Alice', 'doctor')");
            statement.executeUpdate("INSERT INTO " + tableName + " (name, description) VALUES ('Bob', 'engineer')");
        }
    }
//NB : le test passe mais il faut le fragmenter en plusieurs tests car trop long
    @Test
    void testAlterTableAddNumFields() throws SQLException {
        // Appeler la méthode à tester
        DatabaseUtils.alterTableAddNumFields(connection, tableName);

        // Vérifier si les colonnes _num ont été ajoutées
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet rs = metaData.getColumns(null, null, tableName, "%_num")) {
            boolean hasNameNum = false;
            boolean hasDescriptionNum = false;

            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                if (columnName.equals("name_num")) hasNameNum = true;
                if (columnName.equals("description_num")) hasDescriptionNum = true;
            }

            assertTrue(hasNameNum, "La colonne name_num doit exister");
            assertTrue(hasDescriptionNum, "La colonne description_num doit exister");
        }

        // Vérifier si les valeurs hashées sont bien générées
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name, name_num, description, description_num FROM " + tableName)) {

            while (rs.next()) {
                String name = rs.getString("name");
                String description = rs.getString("description");
                String nameNum = rs.getString("name_num");
                String descriptionNum = rs.getString("description_num");

                if (name != null) {
                    assertNotNull(nameNum, "Le hash de name ne doit pas être null");
                } else {
                    assertNull(nameNum, "Si name est null, name_num doit être null");
                }

                if (description != null) {
                    assertNotNull(descriptionNum, "Le hash de description ne doit pas être null");
                } else {
                    assertNull(descriptionNum, "Si description est null, description_num doit être null");
                }
            }
        }
    }

    @AfterAll
    void cleanup() throws SQLException {
        // Nettoyer la base après les tests
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DROP TABLE IF EXISTS " + tableName);
        }
        connection.close();
    }
}
