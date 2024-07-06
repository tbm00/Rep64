package dev.tbm00.spigot.rep64;

import dev.tbm00.spigot.rep64.data.MySQLConnection;
import dev.tbm00.spigot.rep64.listener.PlayerJoinLeave;
import dev.tbm00.spigot.rep64.command.RepCommand;
import dev.tbm00.spigot.rep64.command.RepAdminCommand;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class Rep64 extends JavaPlugin {

    private MySQLConnection mysqlConnection;
    private RepManager repManager;

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
        FileConfiguration fileConfig = this.getConfig();

        // Connect to MySQL
        this.mysqlConnection = new MySQLConnection(fileConfig);

        // Connect RepManager
        this.repManager = new RepManager(this.mysqlConnection/*, this */);

        // Register Listener
        getServer().getPluginManager().registerEvents(new PlayerJoinLeave(this.repManager), this);

        // Register Commands
        getCommand("rep").setExecutor(new RepCommand(this.repManager));
        getCommand("repadmin").setExecutor(new RepAdminCommand(this.repManager));
    }

    @Override
    public void onDisable() {
        this.mysqlConnection.closeConnection();
    }

    public MySQLConnection getDatabase() {
        return mysqlConnection;
    }

    public RepManager getRepManager() {
        return repManager;
    }

    private void log(String... strings) {
		for (String s : strings)
			getLogger().info(s);
	}
}