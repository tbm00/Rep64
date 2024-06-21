package dev.tbm00.spigot.rep64;

import dev.tbm00.spigot.rep64.data.MySQLConnection;
import dev.tbm00.spigot.rep64.listener.PlayerJoinLeave;
import dev.tbm00.spigot.rep64.command.RepCommand;

import org.bukkit.command.PluginCommand;
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

        // Connect to MySQL
        this.database = new MySQLConnection(fileConfiguration); 
        try {
            this.database.initializeDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Could not initialize database.");
        }

        // Register Listener
        getServer().getPluginManager().registerEvents(new PlayerJoinLeave(database), this);

        // Register Commands
        RepCommand repCommand = new RepCommand(this.database);
        PluginCommand rep = this.getCommand("rep");
        rep.setTabCompleter(repCommand);
        rep.setExecutor(repCommand);
    }

    @Override
    public void onDisable() {
    }

    private void log(String... strings) {
		for (String s : strings)
			getLogger().info(s);
	}
}