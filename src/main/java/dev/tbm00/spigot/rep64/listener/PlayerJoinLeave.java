package dev.tbm00.spigot.rep64.listener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import dev.tbm00.spigot.rep64.RepManager;
import dev.tbm00.spigot.rep64.model.PlayerEntry;
import dev.tbm00.spigot.rep64.model.CommandEntry;

public class PlayerJoinLeave implements Listener {
    private final JavaPlugin javaPlugin;
    private final ConsoleCommandSender console;
    private final RepManager repManager;
    private final List<CommandEntry> commandEntries;
    private final Boolean commandEntriesEnabled;

    public PlayerJoinLeave(JavaPlugin javaPlugin, RepManager repManager) {
        this.javaPlugin = javaPlugin;
        this.console = Bukkit.getServer().getConsoleSender();
        this.repManager = repManager;
        commandEntries = new ArrayList<>();
        commandEntriesEnabled = loadJoinCommands();
    }

    private boolean loadJoinCommands() {
        ConfigurationSection logicCommandEntries = javaPlugin.getConfig().getConfigurationSection("logicCommandEntries");
        if (logicCommandEntries != null) {
            if (logicCommandEntries.getBoolean("enabled")==false) {
                return false;
            }
            for (String key : logicCommandEntries.getKeys(false)) {
                ConfigurationSection commandSection = logicCommandEntries.getConfigurationSection(key);
                if (commandSection != null) {
                    
                    String leftOperand = commandSection.getString("leftOperand");
                    String operator = commandSection.getString("operator");
                    double rightOperand = commandSection.getDouble("rightOperand");
                    String command = commandSection.getString("command");
                    
                    if (leftOperand != null && operator != null && command != null) {
                        CommandEntry entry = new CommandEntry(leftOperand, operator, rightOperand, command);
                        commandEntries.add(entry);
                        javaPlugin.getLogger().info("Loaded join listener command: " + command);
                    } else {
                        javaPlugin.getLogger().warning("Error: One of the join listener command entries is poorly defined.");
                    }
                }
            }
            return true;
        } else {
            javaPlugin.getLogger().severe("Error: logicCommandEntries section is missing in the config.yml.");
            return false;
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        //long tickDelay = 20;
        BukkitScheduler scheduler = javaPlugin.getServer().getScheduler();

        // run task later
        scheduler.runTaskAsynchronously(javaPlugin, () -> {
            Player player = event.getPlayer();
            PlayerEntry targetPlayerEntry = loadPlayerEntryFromData(player);
            targetPlayerEntry.setLastLogin(new Date());
            repManager.savePlayerEntry(targetPlayerEntry);
            repManager.loadPlayerCache(player.getName());

            // join listener commands
            if (commandEntriesEnabled) {
                processJoinCommands(player, targetPlayerEntry);
            }
        });
    }

    public PlayerEntry loadPlayerEntryFromData(Player player) {
        PlayerEntry playerEntry = repManager.getPlayerEntry(player.getUniqueId().toString());
        if (playerEntry == null) {
            playerEntry = new PlayerEntry(player.getUniqueId().toString(), player.getName(), 0.0, 0.0, 0, 5.0, 0.0, 0, new Date(), new Date());
            repManager.savePlayerEntry(playerEntry);
            javaPlugin.getLogger().info("Could not retrieve player entry... Creating new one!");
        }
        return playerEntry;
    }

    private void processJoinCommands(Player player, PlayerEntry targetPlayerEntry) {
        for (CommandEntry entry : commandEntries) {
            String command = entry.getCommand()
                .replace("<player>", player.getName())
                .replace("<trueAvg>", String.format("%.1f", targetPlayerEntry.getRepAverage()))
                .replace("<shownAvg>", String.format("%.1f", targetPlayerEntry.getRepShown()));

            double targetValue = "shownAvg".equalsIgnoreCase(entry.getLeftOperand()) 
                ? targetPlayerEntry.getRepShown() 
                : targetPlayerEntry.getRepAverage();

            if (evaluateCondition(targetValue, entry.getOperator(), entry.getRightOperand())) {
                javaPlugin.getLogger().info("Running command: " + command);
                Bukkit.dispatchCommand(console, command);
            }
        }
    }

    private boolean evaluateCondition(double targetValue, String operator, double rightOperand) {
        switch (operator) {
            case "==": return targetValue == rightOperand;
            case "!=": return targetValue != rightOperand;
            case ">": return targetValue > rightOperand;
            case ">=": return targetValue >= rightOperand;
            case "<": return targetValue < rightOperand;
            case "<=": return targetValue <= rightOperand;
            default: return false;
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        BukkitScheduler scheduler = javaPlugin.getServer().getScheduler();
        
        scheduler.runTaskAsynchronously(javaPlugin, () -> {
            try {
                PlayerEntry playerEntry = loadPlayerEntryFromData(player);
                playerEntry.setLastLogout(new Date());
                repManager.savePlayerEntry(playerEntry);
                repManager.unloadPlayerCache(player.getName());
            } catch (Exception e) {
                javaPlugin.getLogger().warning("Error processing player quit: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}