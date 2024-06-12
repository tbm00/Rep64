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

public class Commands implements Listener {
    private final MySQLConnection database;

    public Commands(MySQLConnection database) {
        this.database = database;
    }
}
