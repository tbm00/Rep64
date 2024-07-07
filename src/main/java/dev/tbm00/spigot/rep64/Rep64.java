package dev.tbm00.spigot.rep64;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import dev.tbm00.spigot.rep64.data.CacheManager;
import dev.tbm00.spigot.rep64.data.MySQLConnection;
import dev.tbm00.spigot.rep64.listener.PlayerJoinLeave;
import dev.tbm00.spigot.rep64.command.RepCommand;
import dev.tbm00.spigot.rep64.command.RepAdminCommand;
import dev.tbm00.spigot.rep64.expansion.Rep64PAPI;

public class Rep64 extends JavaPlugin {

    private MySQLConnection mysqlConnection;
    private RepManager repManager;

    @Override
    public void onEnable() {
        // Startup Message
        final PluginDescriptionFile pdf = this.getDescription();
		log(
            ChatColor.DARK_PURPLE + "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-",
            pdf.getName() + " v" + pdf.getVersion() + " created by tbm00",
            ChatColor.DARK_PURPLE + "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-"
		);

        // Load Config
        this.saveDefaultConfig();
        FileConfiguration fileConfig = this.getConfig();

        // Connect to MySQL
        this.mysqlConnection = new MySQLConnection(fileConfig);

        // Connect RepManager
        this.repManager = new RepManager(this.mysqlConnection, fileConfig);

        // Register Listener
        getServer().getPluginManager().registerEvents(new PlayerJoinLeave(this.repManager), this);

        // Register Commands
        getCommand("rep").setExecutor(new RepCommand(this.repManager, fileConfig));
        getCommand("repadmin").setExecutor(new RepAdminCommand(this.repManager, fileConfig));

        // Register Placeholder
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new Rep64PAPI(repManager, fileConfig).register();
        } else {
            getLogger().warning("PlaceholderAPI not found!");
        }

        // Register CacheManager
        new CacheManager(this, this.repManager, fileConfig);
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
            getServer().getConsoleSender().sendMessage(ChatColor.LIGHT_PURPLE + s);
	}
}