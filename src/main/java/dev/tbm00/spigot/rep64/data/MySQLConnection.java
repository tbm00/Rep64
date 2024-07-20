package dev.tbm00.spigot.rep64.data;

import java.sql.*;

import org.bukkit.plugin.java.JavaPlugin;

public class MySQLConnection {
    private Connection connection;
    private JavaPlugin javaPlugin;

    public MySQLConnection(JavaPlugin javaPlugin) {
        this.javaPlugin = javaPlugin;
        openConnection();
        initializeDatabase();
    }

    public void openConnection() {
        try {
            String host = javaPlugin.getConfig().getString("database.host");
            String port = javaPlugin.getConfig().getString("database.port");
            String database = javaPlugin.getConfig().getString("database.database");
            String username = javaPlugin.getConfig().getString("database.username");
            String password = javaPlugin.getConfig().getString("database.password");
            String options = javaPlugin.getConfig().getString("database.options");
            this.connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + options, username, password);
            System.out.println("Connected to MySQL database!");
        } catch (SQLException e) {
            System.out.println("Exception: Could not connect to the database...");
            e.printStackTrace();
        }

    }

    public Connection getConnection() {
        return this.connection;
    }

    public void closeConnection() {
        if (this.connection != null) {
            try {
                this.connection.close();
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

