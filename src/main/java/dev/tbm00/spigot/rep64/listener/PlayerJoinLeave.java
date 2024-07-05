package dev.tbm00.spigot.rep64.listener;

import dev.tbm00.spigot.rep64.RepManager;
import dev.tbm00.spigot.rep64.model.PlayerEntry;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Date;

public class PlayerJoinLeave implements Listener {
    private final RepManager repManager;

    public PlayerJoinLeave(RepManager repManager) {
        this.repManager = repManager;
    }

    public PlayerEntry loadPlayerEntryFromData(Player player) {
        PlayerEntry playerEntry = repManager.getPlayerEntry(player.getUniqueId().toString());

        if (playerEntry == null) {
            playerEntry = new PlayerEntry(player.getUniqueId().toString(), player.getName(), 0.0, 0.0, 0, 5.0, 0.0, 0, new Date(), new Date());
            repManager.savePlayerEntry(playerEntry);
            return playerEntry;
        } else {
            System.out.println("Error: Could not retrieve or create player entry...");
            return null;
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        PlayerEntry playerEntry = loadPlayerEntryFromData(p);
        playerEntry.setLastLogin(new Date());
        repManager.savePlayerEntry(playerEntry);
        repManager.loadPlayerCache(p.getName());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        Player p = event.getPlayer();
        PlayerEntry playerEntry = loadPlayerEntryFromData(p);
        playerEntry.setLastLogout(new Date());
        repManager.savePlayerEntry(playerEntry);
        repManager.unloadPlayerCache(p.getName());
    }
}


