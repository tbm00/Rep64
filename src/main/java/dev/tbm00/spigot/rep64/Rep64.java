package dev.tbm00.spigot.rep64;

import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

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
        final PluginDescriptionFile pdf = getDescription();
		log(
            ChatColor.DARK_PURPLE + "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-",
            pdf.getName() + " v" + pdf.getVersion() + " created by tbm00",
            ChatColor.DARK_PURPLE + "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-"
		);

        // Load Config
        saveDefaultConfig();

        // Connect to MySQL
        try {
            mysqlConnection = new MySQLConnection(this);
        } catch (Exception e) {
            getLogger().severe("Failed to connect to MySQL. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Connect RepManager
        repManager = new RepManager(this, mysqlConnection);

        // Register Listener
        getServer().getPluginManager().registerEvents(new PlayerJoinLeave(this, repManager), this);

        // Register Commands
        getCommand("rep").setExecutor(new RepCommand(this, repManager));
        getCommand("repadmin").setExecutor(new RepAdminCommand(this, repManager));

        // Register Placeholder
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new Rep64PAPI(this, repManager).register();
        } else {
            getLogger().warning("PlaceholderAPI not found!");
        }
    }

    @Override
    public void onDisable() {
        mysqlConnection.closeConnection();
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