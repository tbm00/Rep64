package dev.tbm00.spigot.rep64;

import dev.tbm00.spigot.rep64.db.MySQLConnection;
import dev.tbm00.spigot.rep64.listeners.Commands;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;

public class Rep64 extends JavaPlugin {

    private MySQLConnection database;

    @Override
    public void onEnable() {

        System.out.println("Plugin started...");

        //JDBC - Java Database Connectivity API
        this.database = new MySQLConnection();
        try {
            this.database.initializeDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Could not initialize database.");
        }

        getServer().getPluginManager().registerEvents(new Commands(database), this);

    }

    @Override
    public void onDisable() {
        getLogger().info("Goodbye, Console!");
    }

}