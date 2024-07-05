package dev.tbm00.spigot.rep64.data;


import dev.tbm00.spigot.rep64.Rep64;
import java.sql.*;

public class MySQLConnection {

    private Connection connection;
    private final Rep64 rep64;

    public MySQLConnection(Rep64 rep64) {
        this.rep64 = rep64;
        openConnection();
        initializeDatabase();
    }

    public void openConnection() {
        try {
            String host = rep64.getConfig().getString("database.host");
            String port = rep64.getConfig().getString("database.port");
            String database = rep64.getConfig().getString("database.name");
            String username = rep64.getConfig().getString("database.username");
            String password = rep64.getConfig().getString("database.password");
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
            System.out.println("Connected to MySQL database!");
        } catch (SQLException e) {
            System.out.println("Exception: Could not connect to the database...");
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Disconnected from database.");
            } catch (SQLException e) {
                System.out.println("Exception: Could not close the database connection...");
                e.printStackTrace();
            }
        }
    }

    public void initializeDatabase() {
        try (Statement statement = getConnection().createStatement()) {
            String playersTable = "CREATE TABLE IF NOT EXISTS rep64_players (uuid VARCHAR(36) PRIMARY KEY, username VARCHAR(16), rep_avg DOUBLE, rep_avg_last DOUBLE, rep_staff_modifier INT, rep_shown DOUBLE, rep_shown_last DOUBLE, rep_count INT, last_login DATE, last_logout DATE)";
            String repsTable = "CREATE TABLE IF NOT EXISTS rep64_reps (id INT AUTO_INCREMENT PRIMARY KEY, initiator_UUID VARCHAR(36), receiver_UUID VARCHAR(36), rep INT)";
            statement.execute(playersTable);
            statement.execute(repsTable);
        } catch (SQLException e) {
            System.out.println("Exception: Could not initialize the database...");
            e.printStackTrace();
        }
    }
}

