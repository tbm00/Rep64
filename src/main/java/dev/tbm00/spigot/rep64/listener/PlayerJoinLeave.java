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

import dev.tbm00.spigot.rep64.RepManager;
import dev.tbm00.spigot.rep64.model.PlayerEntry;
import dev.tbm00.spigot.rep64.model.CommandEntry;

public class PlayerJoinLeave implements Listener {
    private final ConsoleCommandSender console;
    private final RepManager repManager;
    private final List<CommandEntry> commandEntries;
    private final Boolean commandEntriesEnabled;

    public PlayerJoinLeave(JavaPlugin javaPlugin, RepManager repManager) {
        this.console = Bukkit.getServer().getConsoleSender();
        this.repManager = repManager;
        this.commandEntries = new ArrayList<>();

        ConfigurationSection logicCommandEntries = javaPlugin.getConfig().getConfigurationSection("logicCommandEntries");
        if (logicCommandEntries != null) {
            if (logicCommandEntries.getBoolean("enabled")==false) {
                this.commandEntriesEnabled = false;
                return;
            } else this.commandEntriesEnabled = true;
            for (String key : logicCommandEntries.getKeys(false)) {
                ConfigurationSection commandSection = logicCommandEntries.getConfigurationSection(key);
                if (commandSection != null) {
                    if (commandSection.getBoolean("enabled") == false) continue;
                    
                    String leftOperand = commandSection.getString("leftOperand");
                    String operator = commandSection.getString("operator");
                    double rightOperand = commandSection.getDouble("rightOperand");
                    String command = commandSection.getString("command");
                    
                    if (leftOperand != null && operator != null && command != null) {
                        CommandEntry entry = new CommandEntry(leftOperand, operator, rightOperand, command);
                        commandEntries.add(entry);
                        System.out.println("Loaded join listener command: " + command);
                    } else {
                        System.out.println("Error: One of the join listener command entries is poorly defined.");
                    }
                }
            }
        } else {
            this.commandEntriesEnabled = false;
            System.out.println("Error: logicCommandEntries section is missing in the config.yml.");
        }
    }

    public PlayerEntry loadPlayerEntryFromData(Player player) {
        PlayerEntry playerEntry = repManager.getPlayerEntry(player.getUniqueId().toString());
        if (playerEntry == null) {
            playerEntry = new PlayerEntry(player.getUniqueId().toString(), player.getName(), 0.0, 0.0, 0, 5.0, 0.0, 0, new Date(), new Date());
            repManager.savePlayerEntry(playerEntry);
            System.out.println("Could not retrieve player entry... Creating new one!");
        }
        return playerEntry;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerEntry targetPlayerEntry = loadPlayerEntryFromData(player);
        targetPlayerEntry.setLastLogin(new Date());
        repManager.savePlayerEntry(targetPlayerEntry);
        repManager.loadPlayerCache(player.getName());

        // Join Listener Commands
        if (this.commandEntriesEnabled == false) return;
        for (CommandEntry entry : commandEntries) {
            String leftOperand = entry.getLeftOperand();
            String operator = entry.getOperator();
            Double rightOperand = entry.getRightOperand();
            String command = entry.getCommand();
            if (command != null) {
                command = command
                    .replace("<player>", player.getName())
                    .replace("<trueAvg>", String.format("%.1f", targetPlayerEntry.getRepAverage()))
                    .replace("<shownAvg>", String.format("%.1f", targetPlayerEntry.getRepShown()));
            } else {
                System.out.println("Error: 'command' is null in onPlayerJoin");
                continue;
            }

            double targetValue;
            if (leftOperand.equalsIgnoreCase("shownAvg")) {
                targetValue = targetPlayerEntry.getRepShown();
            } else if (leftOperand.equalsIgnoreCase("trueAvg")) {
                targetValue = targetPlayerEntry.getRepAverage();
            } else {
                System.out.println("Error: leftOperand not correct in a join listener command!");
                continue;
            }
            
            Boolean trigger = false;
            switch (operator) {
                case "==":
                    if (targetValue == rightOperand) trigger = true;
                    break;
                case "!=":
                    if (targetValue != rightOperand) trigger = true;
                    break;
                case ">":
                    if (targetValue > rightOperand) trigger = true;
                    break;
                case ">=":
                    if (targetValue >= rightOperand) trigger = true;
                    break;
                case "<":
                    if (targetValue < rightOperand) trigger = true;
                    break;
                case "<=":
                    if (targetValue <= rightOperand) trigger = true;
                    break;
                default:
                    break;
            }
            if (trigger == true) {
                System.out.println("Running command: " + command);
                Bukkit.dispatchCommand(console, command);
            }
        }
    } 

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        PlayerEntry playerEntry = loadPlayerEntryFromData(player);
        playerEntry.setLastLogout(new Date());
        repManager.savePlayerEntry(playerEntry);
        //repManager.unloadPlayerCache(p.getName());
    }
}


