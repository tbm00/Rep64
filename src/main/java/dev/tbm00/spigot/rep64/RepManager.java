package dev.tbm00.spigot.rep64;

import dev.tbm00.spigot.rep64.data.MySQLConnection;
import dev.tbm00.spigot.rep64.model.PlayerEntry;
import dev.tbm00.spigot.rep64.model.RepEntry;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
//import org.bukkit.plugin.java.JavaPlugin;

public class RepManager {

    private final MySQLConnection db;
    public final Map<String, String> username_map; // key = username
    private final Map<String, PlayerEntry> player_map; // key = UUID
    private final Map<String, Map<String, RepEntry>> rep_map; // key1 = initiatorUUID, key2= receiverUUID
    //private final JavaPlugin plugin;

    public RepManager(MySQLConnection database/* , JavaPlugin plugin*/) {
        this.db = database;
        //this.plugin = plugin;
        this.player_map = new HashMap<>();
        this.rep_map = new HashMap<>();
        this.username_map = new HashMap<>();
    }

    public void reload() {
        // reload config
        //plugin.reloadConfig();
        
        // reload MySQL connection
        db.closeConnection();
        db.openConnection();

        // refresh caches with information for online players
        player_map.clear();
        username_map.clear();
        rep_map.clear();
        for (Player player : Bukkit.getOnlinePlayers()) {
            loadPlayerCache(player.getName());
        }
    }

    public void loadPlayerCache(String username) {
        String UUID = getPlayerUUID(username);
        username_map.put(username, UUID); // username_map

        // player_map
        PlayerEntry playerEntry = getPlayerEntry(UUID);
        if (playerEntry != null) {
            player_map.put(UUID, playerEntry);
        }

        // rep_map - receiver
        try (PreparedStatement statement = db.getConnection()
                .prepareStatement("SELECT * FROM rep64_reps WHERE receiver_UUID = ?")) {
            statement.setString(1, UUID);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String initiatorUUID = resultSet.getString("initiator_UUID");
                    String receiverUUID = resultSet.getString("receiver_UUID");
                    int rep = resultSet.getInt("rep");
                    RepEntry repEntry = new RepEntry(id, initiatorUUID, receiverUUID, rep);
                    
                    rep_map.putIfAbsent(initiatorUUID, new HashMap<>());
                    rep_map.get(initiatorUUID).put(receiverUUID, repEntry);
                }
            }
        } catch (SQLException e) {
            System.out.println("Exception: Could not load rep entries set on the player...");
            e.printStackTrace();
        }

        // rep_map - initiator
        try (PreparedStatement statement = db.getConnection()
                .prepareStatement("SELECT * FROM rep64_reps WHERE initiator_UUID = ?")) {
            statement.setString(1, UUID);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String initiatorUUID = resultSet.getString("initiator_UUID");
                    String receiverUUID = resultSet.getString("receiver_UUID");
                    int rep = resultSet.getInt("rep");
                    RepEntry repEntry = new RepEntry(id, initiatorUUID, receiverUUID, rep);
                    
                    rep_map.putIfAbsent(initiatorUUID, new HashMap<>());
                    rep_map.get(initiatorUUID).put(receiverUUID, repEntry);
                }
            }
        } catch (SQLException e) {
            System.out.println("Exception: Could not load rep entries set by the player...");
            e.printStackTrace();
        }
    }

    public void unloadPlayerCache(String username) {
        String UUID = getPlayerUUID(username);
        username_map.remove(username); // username_map
        player_map.remove(UUID); // player_map

        List<String> onlinePlayersUUIDs = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            onlinePlayersUUIDs.add(player.getUniqueId().toString());
        }

        // rep_map - receiver
        for (Map.Entry<String, Map<String, RepEntry>> entry : rep_map.entrySet()) {
            if (!onlinePlayersUUIDs.contains(entry.getKey())) {
                entry.getValue().remove(UUID);
            }
        }
        // rep_map - initiator
        if (!onlinePlayersUUIDs.contains(UUID)) {
            rep_map.remove(UUID);
        }
    }

    // returns player name from cache(map) first
    // if not found, returns player name from sql
    // if not found, returns null
    public String getPlayerUUID(String username) {
        if (username_map.containsKey(username)) {
            return username_map.get(username); // return UUID from map
        } else {
            try (PreparedStatement statement = db.getConnection()
                    .prepareStatement("SELECT * FROM rep64_players WHERE username = ?")) {
                statement.setString(1, username);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) return resultSet.getString("uuid");
                }
            } catch (SQLException e) {
                System.out.println("Exception: Could not find UUID...");
                e.printStackTrace();
                return null;
            }
            System.out.println("Error: Could not find UUID...");
            return null;
        }
    }

    public String getPlayerUsername(String UUID) {
        try (PreparedStatement statement = db.getConnection()
                .prepareStatement("SELECT * FROM rep64_players WHERE uuid = ?")) {
            statement.setString(1, UUID);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) return resultSet.getString("username");
            }
        } catch (SQLException e) {
            System.out.println("Exception: Could not find username...");
            e.printStackTrace();
            return null;
        }
        System.out.println("Error: Could not find username...");
        return null;
        
    }

    // returns player entry from cache(map) first
    // if not found, returns player entry from sql
    // if not found, returns null
    public PlayerEntry getPlayerEntry(String UUID) {
        if (player_map.containsKey(UUID)) {
            return player_map.get(UUID);
        } else {
            try (PreparedStatement statement = db.getConnection()
                    .prepareStatement("SELECT * FROM rep64_players WHERE uuid = ?")) {
                statement.setString(1, UUID);
                try (ResultSet resultSet = statement.executeQuery()) {
                    PlayerEntry entry;
                    if (resultSet.next()) {
                        entry = new PlayerEntry(resultSet.getString("uuid"),
                                resultSet.getString("username"),
                                resultSet.getDouble("rep_avg"),
                                resultSet.getDouble("rep_avg_last"),
                                resultSet.getInt("rep_staff_modifier"),
                                resultSet.getDouble("rep_shown"),
                                resultSet.getDouble("rep_shown_last"),
                                resultSet.getInt("rep_count"),
                                resultSet.getDate("last_login"),
                                resultSet.getDate("last_logout"));
                        return entry;
                    }
                }
            } catch (SQLException e) {
                System.out.println("Exception: Could not find player entry...");
                e.printStackTrace();
                return null;
            }
            System.out.println("Error: Could not find player entry...");
            return null;
        }
    }

    // saves player entry to cache(map) and database(SQL)
    public void savePlayerEntry(PlayerEntry playerEntryPassed) {
        PlayerEntry playerEntry = calculateRepAverage(playerEntryPassed);
        // check if player entry exists in cache(map)
        if (!player_map.containsKey(playerEntry.getPlayerUUID())) {
            // create new player entry in cache(map)
            player_map.put(playerEntry.getPlayerUUID(), playerEntry);
            username_map.put(playerEntry.getPlayerUsername(), playerEntry.getPlayerUUID());
        } else {
            // update existing entry in cache(map)
            player_map.put(playerEntry.getPlayerUUID(), playerEntry);
            username_map.put(playerEntry.getPlayerUsername(), playerEntry.getPlayerUUID());
        }

        // save to sql
        try (PreparedStatement statement = db.getConnection()
                .prepareStatement("REPLACE INTO rep64_players (uuid, username, rep_avg, rep_avg_last, rep_staff_modifier, rep_shown, rep_shown_last, rep_count, last_login, last_logout) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            statement.setString(1, playerEntry.getPlayerUUID());
            statement.setString(2, playerEntry.getPlayerUsername());
            statement.setDouble(3, playerEntry.getRepAverage());
            statement.setDouble(4, playerEntry.getRepAverageLast());
            statement.setInt(5, playerEntry.getRepStaffModifier());
            statement.setDouble(6, playerEntry.getRepShown());
            statement.setDouble(7, playerEntry.getRepShownLast());
            statement.setInt(8, playerEntry.getRepCount());
            statement.setDate(9, new java.sql.Date(playerEntry.getLastLogin().getTime()));
            statement.setDate(10, new java.sql.Date(playerEntry.getLastLogout().getTime()));
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Exception: Could not save player entry...");
            e.printStackTrace();
        }
    }

    public void deletePlayerEntry(String UUID) {
        // delete rep entries
        deleteRepEntriesByReceiver(UUID);
        deleteRepEntriesByInitiator(UUID);
        

        // remove from local cache(map)
        if (player_map.containsKey(UUID)) {
            player_map.remove(UUID);
        }
    
        // remove from sql
        try (PreparedStatement statement = db.getConnection()
                .prepareStatement("DELETE FROM rep64_players WHERE uuid = ?")) {
            statement.setString(1, UUID);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Exception: Could not delete player entry...");
            e.printStackTrace();
        }
    }

    // returns rep entry from cache(map) first
    // if not found, returns rep entry from sql
    // if not found, returns null
    public RepEntry getRepEntry(String initiatorUUID, String receiverUUID) {
        if (rep_map.containsKey(initiatorUUID) && rep_map.get(initiatorUUID).containsKey(receiverUUID)) {
            return rep_map.get(initiatorUUID).get(receiverUUID);
        } else {
            try (PreparedStatement statement = db.getConnection()
                    .prepareStatement("SELECT * FROM rep64_reps WHERE initiator_UUID = ? AND receiver_UUID = ?")) {
                statement.setString(1, initiatorUUID);
                statement.setString(2, receiverUUID);
                try (ResultSet resultSet = statement.executeQuery()) {
                    RepEntry repEntry = null;
                    if (resultSet.next()) {
                        repEntry = new RepEntry(resultSet.getInt("id"),
                                resultSet.getString("initiator_UUID"),
                                resultSet.getString("receiver_UUID"),
                                resultSet.getInt("rep"));
                    }
                    return repEntry;
                }
            } catch (SQLException e) {
                System.out.println("Exception: Could not find rep entry...");
                e.printStackTrace();
                return null;
            }
        }
    }

    public Set<String> getRepInitiators(String receiverUUID) {
        Set<String> intiatorList = new HashSet<>();
        // get receiver list from SQL
        try (PreparedStatement selectStatement = db.getConnection()
            .prepareStatement("SELECT initiator_UUID FROM rep64_reps WHERE receiver_UUID = ?")) {
            selectStatement.setString(1, receiverUUID);
            try (ResultSet resultSet = selectStatement.executeQuery()) {
                while (resultSet.next()) {
                    intiatorList.add(getPlayerUsername(resultSet.getString("initiator_UUID")));
                }
            }
        } catch (SQLException e) {
            System.out.println("Exception: Could not fetch initiator list...");
            e.printStackTrace();
            return null;
        }
        return intiatorList;
    }

    public Set<String> getRepReceivers(String initiatorUUID) {
        Set<String> receiverList = new HashSet<>();
        // get receiver list from SQL
        try (PreparedStatement selectStatement = db.getConnection()
            .prepareStatement("SELECT receiver_UUID FROM rep64_reps WHERE initiator_UUID = ?")) {
            selectStatement.setString(1, initiatorUUID);
            try (ResultSet resultSet = selectStatement.executeQuery()) {
                while (resultSet.next()) {
                    receiverList.add(getPlayerUsername(resultSet.getString("receiver_UUID")));
                }
            }
        } catch (SQLException e) {
            System.out.println("Exception: Could not fetch receiver list...");
            e.printStackTrace();
            return null;
        }
        return receiverList;
    }

    public void saveRepCount(String UUID) {
        try (PreparedStatement statement = db.getConnection()
                .prepareStatement("SELECT * FROM rep64_reps WHERE receiver_UUID = ?")) {
            statement.setString(1, UUID);
    
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Double> repList = new ArrayList<>();
    
                while(resultSet.next()) {
                    repList.add((double) resultSet.getInt("rep"));
                }
    
                int newCount = 0;
                if (!repList.isEmpty()) {
                    newCount = repList.size();
                }
                // store/save new rep count to cache
                if (player_map.containsKey(UUID)) {
                    PlayerEntry playerEntry = player_map.get(UUID);
                    playerEntry.setRepCount(newCount);
                    player_map.put(UUID, playerEntry);
                }

                // store/save new rep count to sql
                try (PreparedStatement updateStatement = db.getConnection()
                        .prepareStatement("UPDATE rep64_players SET rep_count = ? WHERE UUID = ?")) {
                    updateStatement.setInt(1, newCount);
                    updateStatement.setString(2, UUID);
                    updateStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.out.println("Exception: Could not save rep count...");
            e.printStackTrace();
        }
    }

    public PlayerEntry calculateRepAverage(PlayerEntry entry) {
        String UUID = entry.getPlayerUUID();
        saveRepCount(UUID);
        try (PreparedStatement statement = db.getConnection()
                .prepareStatement("SELECT * FROM rep64_reps WHERE receiver_UUID = ?")) {
            statement.setString(1, UUID);
    
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Double> repList = new ArrayList<>();

                while(resultSet.next()) {
                    repList.add((double) resultSet.getInt("rep"));
                }

                // save prior rep averages
                double lastRepAvg = entry.getRepAverage();
                double lastRepShown = entry.getRepShown();
                entry.setRepAverageLast(lastRepAvg);
                entry.setRepShownLast(lastRepShown);

                // calculate new average
                double newAverage = 5.0;
                if (!repList.isEmpty()) {
                    // calculate new rep average
                    double sum = 0.0;
                    for (double rep : repList) sum += rep;
                    newAverage = sum / repList.size();
                }

                // apply staff modifier and save
                double staffMod = entry.getRepStaffModifier();
                entry.setRepAverage(newAverage);
                entry.setRepShown(newAverage + staffMod);
            }
        } catch (SQLException e) {
            System.out.println("Exception: Could not calculate rep average...");
            e.printStackTrace();
        }
        return entry;
    }

    // saves rep entry to cache(map) and database(SQL)
    public void saveRepEntry(RepEntry repEntry) {
        boolean isNewEntry = (!rep_map.containsKey(repEntry.getInitiatorUUID()) || 
                              !rep_map.get(repEntry.getInitiatorUUID()).containsKey(repEntry.getReceiverUUID()));
    
        if (isNewEntry) {
            // create new rep entry in cache(map)
            rep_map.putIfAbsent(repEntry.getInitiatorUUID(), new HashMap<>());
            rep_map.get(repEntry.getInitiatorUUID()).put(repEntry.getReceiverUUID(), repEntry);
    
            // increase rep_count in cache(map)
            String receiverUUID = repEntry.getReceiverUUID();
            if (player_map.containsKey(receiverUUID)) {
                PlayerEntry playerEntry = player_map.get(receiverUUID);
                playerEntry.setRepCount(playerEntry.getRepCount() + 1);
                player_map.put(receiverUUID, playerEntry);
            }
            // increase rep_count in sql
            try (PreparedStatement updateStatement = db.getConnection()
                    .prepareStatement("UPDATE rep64_players SET rep_count = rep_count + 1 WHERE UUID = ?")) {
                updateStatement.setString(1, receiverUUID);
                updateStatement.executeUpdate();
            } 
            catch (SQLException e) {
                System.out.println("Exception: Could not save increased rep count to SQL...");
                e.printStackTrace();
            }

            // create new rep entry in sql
            try (PreparedStatement statement = db.getConnection()
                    .prepareStatement("INSERT INTO rep64_reps (initiator_UUID, receiver_UUID, rep) VALUES (?, ?, ?)", 
                    Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, repEntry.getInitiatorUUID());
                statement.setString(2, repEntry.getReceiverUUID());
                statement.setInt(3, repEntry.getRep());
                statement.executeUpdate();

                // retrieve generated id
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        repEntry.setId(generatedKeys.getInt(1));
                        rep_map.get(repEntry.getInitiatorUUID()).put(repEntry.getReceiverUUID(), repEntry);
                    }
                }
            } catch (SQLException e) {
                System.out.println("Exception: Could not save rep entry to SQL...");
                e.printStackTrace();
            }
        } else {
            // update existing rep entry in cache(map)
            rep_map.get(repEntry.getInitiatorUUID()).put(repEntry.getReceiverUUID(), repEntry);

            // update existing rep entry in sql
            try (PreparedStatement statement = db.getConnection()
                    .prepareStatement("UPDATE rep64_reps SET rep = ? WHERE initiator_UUID = ? AND receiver_UUID = ?")) {
                statement.setInt(1, repEntry.getRep());
                statement.setString(2, repEntry.getInitiatorUUID());
                statement.setString(3, repEntry.getReceiverUUID());
                statement.executeUpdate();
            } catch (SQLException e) {
            e.printStackTrace();
            }
        }
        // recalculate the average rep
        savePlayerEntry(getPlayerEntry(repEntry.getReceiverUUID()));
    }

    public void deleteRepEntry(String initiatorUUID, String receiverUUID) {
        if (rep_map.containsKey(initiatorUUID) && rep_map.get(initiatorUUID).containsKey(receiverUUID)) {
            // remove rep from rep map
            rep_map.get(initiatorUUID).remove(receiverUUID);

            // if initiator key is empty, remove it
            if (rep_map.get(initiatorUUID).isEmpty()) {
                rep_map.remove(initiatorUUID);
            }
        }

        // delete from sql
        try (PreparedStatement statement = db.getConnection()
            .prepareStatement("DELETE FROM rep64_reps WHERE initiator_UUID = ? AND receiver_UUID = ?")) {
            statement.setString(1, initiatorUUID);
            statement.setString(2, receiverUUID);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Exception: Could not delete rep entry...");
            e.printStackTrace();
        }
        if (receiverUUID!=null) {
            loadPlayerCache(getPlayerUsername(receiverUUID));
            savePlayerEntry(getPlayerEntry(receiverUUID));
            unloadPlayerCache(getPlayerUsername(receiverUUID));
            loadPlayerCache(getPlayerUsername(receiverUUID));
        }
    }

    public void deleteRepEntriesByInitiator(String initiatorUUID) {
        Set<String> receiverList = getRepReceivers(initiatorUUID);
        
        // remove any entry in cache(map)
        if (rep_map.containsKey(initiatorUUID)) {
            rep_map.remove(initiatorUUID);
        }

        // delete from sql
        try (PreparedStatement statement = db.getConnection()
            .prepareStatement("DELETE FROM rep64_reps WHERE initiator_UUID = ?")) {
            statement.setString(1, initiatorUUID);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Exception: Could not delete rep entries...");
            e.printStackTrace();
        }

        // re calculate
        for (String receiverUUID : receiverList) {
            if (receiverUUID!=null) {
                savePlayerEntry(getPlayerEntry(receiverUUID));
                unloadPlayerCache(getPlayerUsername(receiverUUID));
                loadPlayerCache(getPlayerUsername(receiverUUID));
            }
        }
    }

    public void deleteRepEntriesByReceiver(String receiverUUID) {
        // iterate thru outer map to find and remove entries with receiverUUID
        rep_map.values().forEach(innerMap -> innerMap.remove(receiverUUID));

        // remove any outer keys that have empty inner maps
        rep_map.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        
        // delete from sql
        try (PreparedStatement statement = db.getConnection()
            .prepareStatement("DELETE FROM rep64_reps WHERE receiver_UUID = ?")) {
            statement.setString(1, receiverUUID);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Exception: Could not delete rep entries...");
            e.printStackTrace();
        }
        if (receiverUUID!=null) {
            savePlayerEntry(getPlayerEntry(receiverUUID));
            unloadPlayerCache(getPlayerUsername(receiverUUID));
            loadPlayerCache(getPlayerUsername(receiverUUID));
        }
    }
}