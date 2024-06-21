package dev.tbm00.spigot.rep64.listeners;

import dev.tbm00.spigot.rep64.db.MySQLConnection;
import dev.tbm00.spigot.rep64.model.PlayerEntry;
import dev.tbm00.spigot.rep64.model.RepEntry;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;
import java.util.Date;

public class RepListener implements Listener {
    private final MySQLConnection database;

    public RepListener(MySQLConnection database) {
        this.database = database;
    }

    public PlayerEntry getPlayerEntryFromDatabase(Player player) throws SQLException {
        PlayerEntry playerEntry = database.getPlayerByUUID(player.getUniqueId().toString());

        if (playerEntry == null) {
            playerEntry = new PlayerEntry(player.getUniqueId().toString(), player.getName(), 5, 0, 0, 0.0, 0, new Date(), new Date());
            database.createPlayerEntry(playerEntry);
        }
        return playerEntry;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        try {
            PlayerEntry playerEntry = getPlayerEntryFromDatabase(p);
            playerEntry.setLastLogin(new Date());
            database.updatePlayerEntry(playerEntry);
        } catch (SQLException e){
            e.printStackTrace();
            System.out.println("Could not update player stats after join.");
        }

    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        Player p = event.getPlayer();
        try {
            PlayerEntry playerEntry = getPlayerEntryFromDatabase(p);
            playerEntry.setLastLogout(new Date());
            database.updatePlayerEntry(playerEntry);
        } catch (SQLException e){
            e.printStackTrace();
            System.out.println("Could not update player stats after quit.");
        }

    }
}
