package dev.tbm00.spigot.rep64.db;

import dev.tbm00.spigot.rep64.model.PlayerEntry;
import dev.tbm00.spigot.rep64.model.RepEntry;
import java.sql.*;

public class MySQLConnection {

    private Connection connection;

    public Connection getConnection() throws SQLException {
        // If connection already established, return it
        if(connection != null){
            return connection;
        }

        String prefix = "jdbc:mysql://";
        String host = "localhost";
        String port = "3306";
        String database = "rep64";
        String options = "?autoReconnect=true";
        String url = prefix + host + ":" + port + "/" + database + options;
        String user = "root";
        String password = "password";

        Connection connection = DriverManager.getConnection(url, user, password);
        this.connection = connection;
        System.out.println("Connected to database.");

        return connection;
    }

    public void initializeDatabase() throws SQLException {
        Statement statement = getConnection().createStatement();

        //Create the tables
        String playersTable = "CREATE TABLE IF NOT EXISTS rep64_players (uuid VARCHAR(36) PRIMARY KEY, username STRING, rep_avg DOUBLE, rep_avg_last DOUBLE, rep_staff_modifier INT, rep_shown DOUBLE, rep_count INT, last_login DATE, last_logout DATE)";
        String repsTable = "CREATE TABLE IF NOT EXISTS rep64_reps (id INT AUTO_INCREMENT PRIMARY KEY, initiatorUUID STRING, receiverUUID STRING, rep INT)";

        statement.execute(playersTable);
        statement.execute(repsTable);
        statement.close();
    }

    public void createPlayerEntry(PlayerEntry player) throws SQLException {
        PreparedStatement statement = getConnection()
                .prepareStatement("INSERT INTO rep64_players(uuid, username, rep_avg, rep_avg_last, rep_staff_modifier, rep_shown, rep_count, last_login, last_logout) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
        statement.setString(1, player.getPlayerUUID());
        statement.setString(2, player.getPlayerUsername());
        statement.setDouble(3, player.getRepAverage());
        statement.setDouble(4, player.getRepAverageLast());
        statement.setInt(5, player.getRepStaffModifier());
        statement.setDouble(6, player.getRepShown());
        statement.setInt(7, player.getRepCount());
        statement.setDate(8, new Date(player.getLastLogin().getTime()));
        statement.setDate(9, new Date(player.getLastLogout().getTime()));

        statement.executeUpdate();
        statement.close();

    }

    public void updatePlayerEntry(PlayerEntry player) throws SQLException {
        PreparedStatement statement = getConnection()
                .prepareStatement("UPDATE rep64_players SET rep_avg = ?, rep_avg_last = ?, rep_staff_modifier = ?, rep_shown = ?, rep_count = ?, last_login = ?, last_logout = ? WHERE player = ?");
        statement.setDouble(1, player.getRepAverage());
        statement.setDouble(2, player.getRepAverageLast());
        statement.setInt(3, player.getRepStaffModifier());
        statement.setDouble(4, player.getRepShown());
        statement.setInt(5, player.getRepCount());
        statement.setDate(6, new Date(player.getLastLogin().getTime()));
        statement.setDate(7, new Date(player.getLastLogout().getTime()));
        statement.setString(8, player.getPlayerUsername());

        statement.executeUpdate();
        statement.close();

    }

    public void deletePlayerEntry(PlayerEntry player) throws SQLException {
        PreparedStatement statement = getConnection()
                .prepareStatement("DELETE FROM rep64_players WHERE uuid = ?");
        statement.setString(1, player.getPlayerUUID());

        statement.executeUpdate();
        statement.close();
    }

    public PlayerEntry findPlayerByUsername(String username) throws SQLException {
        PreparedStatement statement = getConnection()
                .prepareStatement("SELECT * FROM rep64_players WHERE username = ?");
        statement.setString(1, username);

        ResultSet resultSet = statement.executeQuery();
        PlayerEntry player;

        if(resultSet.next()){
            player = new PlayerEntry(resultSet.getString("uuid"),
                    resultSet.getString("username"),
                    resultSet.getDouble("rep_avg"),
                    resultSet.getDouble("rep_avg_last"),
                    resultSet.getInt("rep_staff_modifier"),
                    resultSet.getDouble("rep_shown"),
                    resultSet.getInt("rep_count"),
                    resultSet.getDate("last_login"),
                    resultSet.getDate("last_logout"));
            statement.close();
            return player;
        }
        statement.close();
        return null;
    }
}

