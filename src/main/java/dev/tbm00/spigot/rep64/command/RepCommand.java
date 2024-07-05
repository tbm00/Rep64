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
    private final String prefix = ChatColor.DARK_GRAY + "[" + ChatColor.WHITE + "Rep" + ChatColor.DARK_GRAY + "] " + ChatColor.RESET;

    public RepCommand(RepManager repManager) {
        this.repManager = repManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // /rep
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(prefix + ChatColor.RED + "This command can only be run by a player.");
                return true;
            }
            Player initiator = (Player) sender;
            PlayerEntry playerEntry = repManager.getPlayerEntry(initiator.getUniqueId().toString());
            if (playerEntry != null) {
                double repShown = playerEntry.getRepShown();
                initiator.sendMessage(ChatColor.GRAY + "Your reputation: " + ChatColor.LIGHT_PURPLE + String.format("%.1f", repShown) 
                    + ChatColor.DARK_GRAY + " (avg of " + playerEntry.getRepCount() + " entries)");
            } else {
                initiator.sendMessage(ChatColor.RED + "An error occurred while fetching your reputation!");
            }
        }

        // /rep <>
        if (args.length == 1) {
            if ("reload".equals(args[0])) {
                if (sender.hasPermission("rep64.reload")) {
                    try {
                        repManager.reload();
                        sender.sendMessage(prefix + ChatColor.GREEN + "You successfully reloaded the plugin!");
                    } catch (Exception e){
                        System.out.println("Error connecting database when reloading!");
                    }
                }
                else {
                    sender.sendMessage(prefix + ChatColor.RED + "No permission!");
                }
            }
            else {
                Player initiator = (Player) sender;
                String targetName = args[0];
                PlayerEntry targetEntry = repManager.getPlayerEntry(repManager.getPlayerUUID(targetName));
                if (targetEntry != null) {
                    double targetRepShown = targetEntry.getRepShown();
                    initiator.sendMessage(ChatColor.GRAY + targetName + " reputation: " + ChatColor.LIGHT_PURPLE + String.format("%.1f", targetRepShown) 
                        + ChatColor.DARK_GRAY + " (avg of " + targetEntry.getRepCount() + " entries)");
                } else {
                    initiator.sendMessage(ChatColor.RED + "An error occurred while fetching their reputation!");
                }
            }
        }
        
        // /rep <> <>
        if (args.length == 2) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(prefix + ChatColor.RED + "This command can only be run by a player!");
                return true;
            }
            if (!sender.hasPermission("rep64.set")) {
                sender.sendMessage(prefix + ChatColor.RED + "No permission!");
                return true;
            }

            Player initiator = (Player) sender;
            String targetName = args[0];
            String action = args[1];
            //int rep;

            String targetUUID = repManager.getPlayerUUID(targetName);
            PlayerEntry targetPlayerEntry = repManager.getPlayerEntry(targetUUID);
            if (action.equalsIgnoreCase("unset")) {
                if (targetPlayerEntry != null) {
                    String initiatorUUID = initiator.getUniqueId().toString();
                    String receiverUUID = targetPlayerEntry.getPlayerUUID();
                    repManager.deleteRepEntry(initiatorUUID, receiverUUID);
                    sender.sendMessage(prefix + ChatColor.GREEN + "You have removed your reputation entry for " + targetName + "!");
                } else {
                    sender.sendMessage(prefix + ChatColor.RED + "Error: Player data is not available!");
                }
            } else {
                try {
                    int rep = Integer.parseInt(args[1]);
                    if (rep < 0 || rep > 10) {
                        sender.sendMessage(prefix + ChatColor.RED + "The reputation value must be between 0-10 (or 'unset')!");
                        return true;
                    }

                    // re-calculate current
                    repManager.calculateRepAverage(targetPlayerEntry.getPlayerUUID());
                    RepEntry targetRepEntry = new RepEntry(initiator.getUniqueId().toString(), targetUUID, rep);

                    // save/create new rep entry in databases (sql and cache)
                    repManager.saveRepEntry(targetRepEntry);
                    repManager.calculateRepAverage(targetPlayerEntry.getPlayerUUID());
                    
                    // message player
                    sender.sendMessage(prefix + ChatColor.GREEN + "You have given " + repManager.getPlayerEntry(targetRepEntry.getReceiverUUID()).getPlayerUsername() 
                        + " a reputation of " + targetRepEntry.getRep());
                    sender.sendMessage(prefix + ChatColor.GRAY + targetPlayerEntry.getPlayerUsername() + "'s Rep: " 
                        + ChatColor.RED + String.format("%.1f", targetPlayerEntry.getRepShownLast())
                        + ChatColor.DARK_GRAY + " (avg of " + (targetPlayerEntry.getRepCount()-1) + " entries)");
                    sender.sendMessage(prefix + ChatColor.GRAY + targetPlayerEntry.getPlayerUsername() + "'s Rep: " 
                        + ChatColor.LIGHT_PURPLE + String.format("%.1f", targetPlayerEntry.getRepShown()) 
                        + ChatColor.DARK_GRAY + " (avg of " + targetPlayerEntry.getRepCount() + " entries)");
                } catch (NumberFormatException e) {
                    sender.sendMessage(prefix + ChatColor.RED + "The reputation value must be between 0-10 (or 'unset')!");
                    return true;
                }
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
