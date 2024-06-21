package dev.tbm00.spigot.rep64.data;

import dev.tbm00.spigot.rep64.model.PlayerEntry;
import dev.tbm00.spigot.rep64.model.RepEntry;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;

public class MySQLConnection {

    private Connection connection;
    private final FileConfiguration fileConfiguration;

    public MySQLConnection(FileConfiguration fileConfiguration) {
        this.fileConfiguration = fileConfiguration;
    }

    public Connection getConnection() throws SQLException {
        // If connection already established, close it
        if(connection != null){
            connection.close();
        }

        String prefix = "jdbc:mysql://";
        String host = fileConfiguration.getString("host");
        String port = fileConfiguration.getString("port");
        String database = fileConfiguration.getString("database");
        String options = fileConfiguration.getString("options");
        String url = prefix + host + ":" + port + "/" + database + options;
        String user = fileConfiguration.getString("username");
        String password = fileConfiguration.getString("password");

        Connection connection = DriverManager.getConnection(url, user, password);
        this.connection = connection;
        System.out.println("Connected to database.");

        return connection;
    }

    public void initializeDatabase() throws SQLException {
        Statement statement = getConnection().createStatement();

        //Create the tables
        String playersTable = "CREATE TABLE IF NOT EXISTS rep64_players (uuid VARCHAR(36) PRIMARY KEY, username VARCHAR(16), rep_avg DOUBLE, rep_avg_last DOUBLE, rep_staff_modifier INT, rep_shown DOUBLE, rep_shown_last DOUBLE, rep_count INT, last_login DATE, last_logout DATE)";
        String repsTable = "CREATE TABLE IF NOT EXISTS rep64_reps (id INT AUTO_INCREMENT PRIMARY KEY, initiator_UUID VARCHAR(36), receiver_UUID VARCHAR(36), rep INT)";

        statement.execute(playersTable);
        statement.execute(repsTable);
        statement.close();
    }

    public void createPlayerEntry(PlayerEntry player) throws SQLException {
        PreparedStatement statement = getConnection()
                .prepareStatement("INSERT INTO rep64_players(uuid, username, rep_avg, rep_avg_last, rep_staff_modifier, rep_shown, rep_shown_last, rep_count, last_login, last_logout) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        statement.setString(1, player.getPlayerUUID());
        statement.setString(2, player.getPlayerUsername());
        statement.setDouble(3, player.getRepAverage());
        statement.setDouble(4, player.getRepAverageLast());
        statement.setInt(5, player.getRepStaffModifier());
        statement.setDouble(6, player.getRepShown());
        statement.setDouble(7, player.getRepShownLast());
        statement.setInt(8, player.getRepCount());
        statement.setDate(9, new Date(player.getLastLogin().getTime()));
        statement.setDate(10, new Date(player.getLastLogout().getTime()));

        statement.executeUpdate();
        statement.close();

    }

    public void updatePlayerEntry(PlayerEntry player) throws SQLException {
        PreparedStatement statement = getConnection()
                .prepareStatement("UPDATE rep64_players SET rep_avg = ?, rep_avg_last = ?, rep_staff_modifier = ?, rep_shown = ?, rep_shown_last, rep_count = ?, last_login = ?, last_logout = ? WHERE uuid = ?");
        statement.setDouble(1, player.getRepAverage());
        statement.setDouble(2, player.getRepAverageLast());
        statement.setInt(3, player.getRepStaffModifier());
        statement.setDouble(4, player.getRepShown());
        statement.setDouble(5, player.getRepShownLast());
        statement.setInt(6, player.getRepCount());
        statement.setDate(7, new Date(player.getLastLogin().getTime()));
        statement.setDate(8, new Date(player.getLastLogout().getTime()));
        statement.setString(9, player.getPlayerUUID());

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

    public PlayerEntry getPlayerByUsername(String username) throws SQLException {
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
                    resultSet.getDouble("rep_shown_last"),
                    resultSet.getInt("rep_count"),
                    resultSet.getDate("last_login"),
                    resultSet.getDate("last_logout"));
            statement.close();
            return player;
        }
        statement.close();
        return null;
    }

    public PlayerEntry getPlayerByUUID(String uuid) throws SQLException {
        PreparedStatement statement = getConnection()
                .prepareStatement("SELECT * FROM rep64_players WHERE uuid = ?");
        statement.setString(1, uuid);

        ResultSet resultSet = statement.executeQuery();
        PlayerEntry player;

        if(resultSet.next()){
            player = new PlayerEntry(resultSet.getString("uuid"),
                    resultSet.getString("username"),
                    resultSet.getDouble("rep_avg"),
                    resultSet.getDouble("rep_avg_last"),
                    resultSet.getInt("rep_staff_modifier"),
                    resultSet.getDouble("rep_shown"),
                    resultSet.getDouble("rep_shown_last"),
                    resultSet.getInt("rep_count"),
                    resultSet.getDate("last_login"),
                    resultSet.getDate("last_logout"));
            statement.close();
            return player;
        }
        statement.close();
        return null;
    }

    public RepEntry getRepEntry(String initiatorUUID, String receiverUUID) throws SQLException {
        PreparedStatement statement = getConnection()
                .prepareStatement("SELECT * FROM rep64_reps WHERE initiator_UUID = ? AND receiver_UUID = ?");
        statement.setString(1, initiatorUUID);
        statement.setString(2, receiverUUID);
        ResultSet resultSet = statement.executeQuery();
        RepEntry repEntry = null;
        if (resultSet.next()) {
            repEntry = new RepEntry(resultSet.getInt("id"),
                    resultSet.getString("initiator_UUID"),
                    resultSet.getString("receiver_UUID"),
                    resultSet.getInt("rep"));
        }
        statement.close();
        return repEntry;
    }

    public double calculateRepAverage(String UUID) throws SQLException {
        PreparedStatement statement = getConnection()
                .prepareStatement("SELECT * FROM rep64_reps WHERE receiver_UUID = ?");
        statement.setString(1, UUID);

        ResultSet repResultSet = statement.executeQuery();
        List<Double> repList = new ArrayList<>();

        while(repResultSet.next()) {
            repList.add((double) repResultSet.getInt("rep"));
        }
        statement.close();

        if (repList.isEmpty()) return 5.0;

        double sum = 0.0;
        for (double rep : repList) sum += rep;

        return (sum / repList.size());
    }

    public double calculateRepShown(String UUID) throws SQLException {
        PlayerEntry player = getPlayerByUUID(UUID);

        return calculateRepAverage(UUID) + player.getRepStaffModifier();
    }

    public void createRepEntry(String initiatorUUID, String receiverUUID, int rep) throws SQLException {
        PreparedStatement statement = getConnection()
                .prepareStatement("INSERT INTO rep64_reps(initiator_UUID, receiver_UUID, rep) VALUES (?, ?, ?)");
        statement.setString(1, initiatorUUID);
        statement.setString(2, receiverUUID);
        statement.setInt(3, rep);
        statement.executeUpdate();
        statement.close();
    }
    
    public void updateRepEntry(String initiatorUUID, String receiverUUID, int rep) throws SQLException {
        PreparedStatement statement = getConnection()
                .prepareStatement("UPDATE rep64_reps SET rep = ? WHERE initiator_UUID = ? AND receiver_UUID = ?");
        statement.setInt(1, rep);
        statement.setString(2, initiatorUUID);
        statement.setString(3, receiverUUID);
        statement.executeUpdate();
        statement.close();
    }

    public void deleteRepEntry(String initiatorUUID, String receiverUUID) throws SQLException {
        PreparedStatement statement = getConnection()
                .prepareStatement("DELETE FROM rep64_reps WHERE initiator_UUID = ? AND receiver_UUID = ?");
        statement.setString(1, initiatorUUID);
        statement.setString(2, receiverUUID);
        statement.executeUpdate();
        statement.close();
    }
    
    public void deleteAllRepEntriesByInitiator(String initiatorUUID) throws SQLException {
        PreparedStatement statement = getConnection()
                .prepareStatement("DELETE FROM rep64_reps WHERE initiator_UUID = ?");
        statement.setString(1, initiatorUUID);
        statement.executeUpdate();
        statement.close();
    }
    
    public void deleteAllRepEntriesByReceiver(String receiverUUID) throws SQLException {
        PreparedStatement statement = getConnection()
                .prepareStatement("DELETE FROM rep64_reps WHERE receiver_UUID = ?");
        statement.setString(1, receiverUUID);
        statement.executeUpdate();
        statement.close();
    }
}

