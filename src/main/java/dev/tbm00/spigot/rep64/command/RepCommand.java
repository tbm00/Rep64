package dev.tbm00.spigot.rep64.command;

import dev.tbm00.spigot.rep64.data.MySQLConnection;
import dev.tbm00.spigot.rep64.model.PlayerEntry;
import dev.tbm00.spigot.rep64.model.RepEntry;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class RepCommand implements TabExecutor {
    private MySQLConnection database;
    private String[] subCommands = new String[]{""};

    public RepCommand(MySQLConnection database) {
        this.database = database;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
                return true;
            }
            Player initiator = (Player) sender;
            try {
                PlayerEntry playerEntry = database.getPlayerByUUID(initiator.getUniqueId().toString());
                double repShown = playerEntry.getRepShown();
                initiator.sendMessage(ChatColor.GREEN + "Your reputation: " + repShown);
            } catch (Exception e) {
                initiator.sendMessage(ChatColor.RED + "An error occurred while fetching your reputation.");
                e.printStackTrace();
            }
        }

        if (args.length == 1) {
            if ("reload".equals(args[0])) {
                if (sender.hasPermission("rep64.reload")) {
                    try {
                        database.getConnection();
                        sender.sendMessage(ChatColor.GREEN + "You successfully reloaded the plugin!");
                    } catch (Exception e){
                        System.out.println("Error connecting database when reloading!");
                    }
                }
                else {
                    sender.sendMessage(ChatColor.RED + "No permission!");
                }
            }
            else {
                Player initiator = (Player) sender;
                String targetName = args[0];
                try { 
                    if (database.getPlayerByUsername(targetName) == null || targetName == null) {
                        initiator.sendMessage(ChatColor.RED + "An error occurred while fetching their reputation.");
                        return true;
                    } else {
                        try {
                            Double targetRepShown = database.getPlayerByUUID(targetName).getRepShown();
                            initiator.sendMessage(ChatColor.GREEN + targetName + " reputation: " + targetRepShown);
                        } catch (Exception e) {
                            initiator.sendMessage(ChatColor.RED + "An exception occurred while fetching their reputation.");
                            e.printStackTrace();
                            return true;
                        }
                    }
                } catch (Exception e) {
                    initiator.sendMessage(ChatColor.RED + "An exception occurred while fetching their reputation.");
                    e.printStackTrace();
                    return true;
                }
            }
        }
        
        if (args.length == 2) {
            Player initiator = (Player) sender;
            String targetName = args[0];
            int rep;
            if (!(sender.hasPermission("rep64.set"))) return true;

            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
                return true;
            }

            try {
                rep = Integer.parseInt(args[1]);
                if (!(-1 < rep && rep < 11)) {
                    sender.sendMessage(ChatColor.RED + "The reputation value must be between 0-10.");
                }
            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "The reputation value must be a number.");
                return true;
            }

            try { 
                if (database.getPlayerByUsername(targetName) == null || targetName == null) {
                    initiator.sendMessage(ChatColor.RED + "An error occurred while fetching their data.");
                    return true;
                }
            } catch (Exception e) {
                initiator.sendMessage(ChatColor.RED + "An exception occurred while fetching their data.");
                e.printStackTrace();
                return true;
            }
            
            try {
                // get current info
                PlayerEntry targetEntry = database.getPlayerByUsername(targetName);
                double currentRepShown = targetEntry.getRepShown();
                double currentRepAverage = targetEntry.getRepAverage();

                // Update database
                database.createRepEntry(initiator.getUniqueId().toString(), database.getPlayerByUsername(targetName).getPlayerUUID(), rep);
                
                // Calculate new Reps
                double newRepShown = database.calculateRepShown(database.getPlayerByUsername(targetName).getPlayerUUID());
                double newRepAverage = database.calculateRepAverage(database.getPlayerByUsername(targetName).getPlayerUUID());

                // Continue updating database
                targetEntry.setRepCount(targetEntry.getRepCount() + 1);
                targetEntry.setRepShown(newRepShown);
                targetEntry.setRepAverage(newRepAverage);
                targetEntry.setRepShownLast(currentRepShown);
                targetEntry.setRepAverageLast(currentRepAverage);
                
                // Message player
                sender.sendMessage(ChatColor.GREEN + "You have given " + targetName + " a reputation of " + rep);
                sender.sendMessage(ChatColor.WHITE + "Average Rep: " + newRepShown + ". Prior Average: " + currentRepShown + ". " + targetEntry.getRepCount() + " Reps.");
            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "An error occurred while creating the reputation entry.");
                e.printStackTrace();
            }
            return true;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(args[0])) {
                    list.add(subCommand);
                }
            }
        }
        return list;
    }
}
