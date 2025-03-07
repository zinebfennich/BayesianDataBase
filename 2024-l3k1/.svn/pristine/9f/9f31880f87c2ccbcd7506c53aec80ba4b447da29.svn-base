package database;

import java.sql.*;

/**
 * classe qui gère la connexion à la base de données.
 */
public class DatabaseConnection {

    private String jdbcUrl;
    private String username;
    private String password;
    private Connection connection;

    public DatabaseConnection(String jdbcUrl, String username, String password) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(jdbcUrl, username, password);
        }
        return connection;
    }

    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}

