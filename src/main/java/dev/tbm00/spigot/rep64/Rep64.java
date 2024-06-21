package dev.tbm00.spigot.rep64;

import dev.tbm00.spigot.rep64.db.MySQLConnection;
import dev.tbm00.spigot.rep64.listeners.RepListener;
//import dev.tbm00.spigot.rep64.model.*;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import java.sql.*;

public class Rep64 extends JavaPlugin {

    private MySQLConnection database;

    @Override
    public void onEnable() {
        // Startup Message
        final PluginDescriptionFile pdf = this.getDescription();
		log(
            "------------------------------",
            pdf.getName() + " by tbm00",
            "------------------------------"
		);

        // Load Config
        this.saveDefaultConfig();
        FileConfiguration fileConfiguration = this.getConfig();

        //JDBC - Java Database Connectivity API
        this.database = new MySQLConnection(fileConfiguration); 
        try {
            this.database.initializeDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Could not initialize database.");
        }

        getServer().getPluginManager().registerEvents(new RepListener(database), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("Goodbye, Console!");
    }

    private void log(String... strings) {
		for (String s : strings)
			getLogger().info(s);
	}
}