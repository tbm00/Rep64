package dev.tbm00.spigot.rep64.command;

import dev.tbm00.spigot.rep64.RepManager;
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
    private RepManager repManager;
    private String[] subCommands = new String[]{""};

    public RepCommand(RepManager repManager) {
        this.repManager = repManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // /rep
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
                return true;
            }
            Player initiator = (Player) sender;
            PlayerEntry playerEntry = repManager.getPlayerEntry(initiator.getUniqueId().toString());
            if (playerEntry != null) {
                double repShown = playerEntry.getRepShown();
                initiator.sendMessage(ChatColor.GREEN + "Your reputation: " + repShown);
            } else {
                initiator.sendMessage(ChatColor.RED + "An error occurred while fetching your reputation.");
            }
        }

        // /rep <>
        if (args.length == 1) {
            if ("reload".equals(args[0])) {
                if (sender.hasPermission("rep64.reload")) {
                    try {
                        repManager.reload();
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
                PlayerEntry targetEntry = repManager.getPlayerEntry(repManager.getPlayerUUID(targetName));
                if (targetEntry != null) {
                    double targetRepShown = targetEntry.getRepShown();
                    initiator.sendMessage(ChatColor.GREEN + targetName + " reputation: " + targetRepShown);
                } else {
                    initiator.sendMessage(ChatColor.RED + "An error occurred while fetching their reputation.");
                }
            }
        }
        
        // /rep <> <>
        if (args.length == 2) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
                return true;
            }
            if (!sender.hasPermission("rep64.set")) {
                sender.sendMessage(ChatColor.RED + "No permission!");
                return true;
            }

            Player initiator = (Player) sender;
            String targetName = args[0];
            int rep;

            try {
                rep = Integer.parseInt(args[1]);
                if (rep < 0 || rep > 10) {
                    sender.sendMessage(ChatColor.RED + "The reputation value must be between 0-10.");
                    return true;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "The reputation value must be a number.");
                return true;
            }

            String targetUUID = repManager.getPlayerUUID(targetName);
            PlayerEntry targetPlayerEntry = repManager.getPlayerEntry(targetUUID);
            if (targetPlayerEntry != null) {
                try {
                    // Re-calculate current
                    repManager.calculateRepAverage(targetPlayerEntry.getPlayerUUID());
    
                    RepEntry targetRepEntry = new RepEntry(initiator.getUniqueId().toString(), targetUUID, rep);

                    // Save/create new rep entry in databases (sql and cache)
                    repManager.saveRepEntry(targetRepEntry);

                    // Save/create new rep entry in databases (sql and cache)
                    repManager.savePlayerEntry(targetPlayerEntry);
                    repManager.calculateRepAverage(targetPlayerEntry.getPlayerUUID());
    
                    // Message player
                    sender.sendMessage(ChatColor.YELLOW + targetPlayerEntry.getPlayerUsername() + "'s Rep: " + targetPlayerEntry.getRepShownLast() + " (avg of " + (targetPlayerEntry.getRepCount()-1) + " entries)");
                    sender.sendMessage(ChatColor.GREEN + "You have given " + targetRepEntry.getReceiverUUID() + " a reputation of " + targetRepEntry.getRep());
                    sender.sendMessage(ChatColor.GREEN + targetPlayerEntry.getPlayerUsername() + "'s Rep: " + targetPlayerEntry.getRepShown() + " (avg of " + targetPlayerEntry.getRepCount() + " entries)");
                } catch (Exception e) {
                    sender.sendMessage(ChatColor.RED + "An error occurred while creating the reputation entry.");
                    e.printStackTrace();
                }
            } else {
                sender.sendMessage(ChatColor.RED + "An error occurred while fetching their data.");
            }
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
