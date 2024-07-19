package dev.tbm00.spigot.rep64.command;

import java.util.List;
import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import dev.tbm00.spigot.rep64.RepManager;
import dev.tbm00.spigot.rep64.model.PlayerEntry;
import dev.tbm00.spigot.rep64.model.RepEntry;

public class RepAdminCommand implements TabExecutor {
    private final RepManager repManager;
    private final String[] subCommands = new String[]{"mod", "show", "deleteRepsBy", "deleteRepsOn", "delete", "reset", "reloadData"};
    private final String prefix = ChatColor.DARK_GRAY + "[" + ChatColor.WHITE + "Rep" + ChatColor.DARK_GRAY + "] " + ChatColor.RESET;
    private final double maxModifier;
    private final double minModifier;
    private final double maxModifierInt;
    private final double minModifierInt;

    public RepAdminCommand(JavaPlugin javaPlugin, RepManager repManager) {
        this.repManager = repManager;
        this.maxModifier = javaPlugin.getConfig().getInt("repScoring.maxModifier");
        this.minModifier = javaPlugin.getConfig().getInt("repScoring.minModifier");
        this.maxModifierInt = javaPlugin.getConfig().getInt("repScoring.maxModifier");
        this.minModifierInt = javaPlugin.getConfig().getInt("repScoring.minModifier");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("rep64.admin")) {
            sender.sendMessage(prefix + ChatColor.RED + "No permission!");
            return false;
        }
        // /repadmin
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(prefix + ChatColor.RED + "This command can only be run by a player!");
                return false;
            }
            sender.sendMessage(ChatColor.DARK_RED + "--- " + ChatColor.RED + "Rep64 Admin Commands" + ChatColor.DARK_RED + " ---\n"
                + ChatColor.WHITE + "/repadmin" + ChatColor.GRAY + " Display this command list\n"
                + ChatColor.WHITE + "/repadmin mod <player> <#>" + ChatColor.GRAY + " Set player's rep modifier (defaults to 0, added to rep avg)\n"
                + ChatColor.WHITE + "/repadmin show <player>" + ChatColor.GRAY + " Display player's rep data\n"
                + ChatColor.WHITE + "/repadmin show <initiator> <receiver>" + ChatColor.GRAY + " Display a specific RepEntry\n"
                + ChatColor.WHITE + "/repadmin delete <initiator> <receiver>" + ChatColor.GRAY + " Delete a specific RepEntry\n"
                + ChatColor.WHITE + "/repadmin deleteRepsBy <initiator>" + ChatColor.GRAY + " Delete RepEntries created by initiator\n"
                + ChatColor.WHITE + "/repadmin deleteRepsOn <receiver>" + ChatColor.GRAY + " Delete RepEntries created on receiver\n"
                + ChatColor.WHITE + "/repadmin reset <player>" + ChatColor.GRAY + " Reset PlayerEntry & delete all associated RepEntries\n"
                + ChatColor.WHITE + "/repadmin reloadData" + ChatColor.GRAY + " Reload MySQL database and plugin's cache\n"
                );
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "mod":
                return handleModCommand(sender, args);
            case "show":
                return handleShowCommand(sender, args);
            case "delete":
                return handleDeleteCommand(sender, args);
            case "deleterepsby":
                return handleDeleteRepsByCommand(sender, args);
            case "deleterepson":
                return handleDeleteRepsOnCommand(sender, args);
            case "reset":
                return handleResetCommand(sender, args);
            case "reloaddata":
                return handleReloadCommand(sender, args);
            default:
                sender.sendMessage(prefix + ChatColor.RED + "Unknown subcommand!");
                return false;
        }
    }

    private boolean handleModCommand(CommandSender sender, String[] args) {
        // /repadmin mod <player> <amount>
        if (args.length != 3) {
            sender.sendMessage(prefix + ChatColor.GRAY + "Usage: /repadmin mod <player> <amount>");
            return false;
        }
        
        int amount;
        try {
            amount = Integer.parseInt(args[2]);
            if (amount < minModifier || amount > maxModifier) {
                sender.sendMessage(prefix + ChatColor.RED + "Invalid amount. It must be between " + minModifierInt + " and " + maxModifierInt + "!");
                return false;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(prefix + ChatColor.RED + "Invalid amount. It must be an integer!");
            return false;
        }

        String targetName = args[1];
        String targetUUID = repManager.getPlayerUUID(targetName);
        PlayerEntry targetPlayerEntry = repManager.getPlayerEntry(targetUUID);
        if (targetPlayerEntry == null) {
            sender.sendMessage(prefix + ChatColor.RED + "Could not find target player!");
            return false;
        }

        // calculate player entry and save to sql & cache
        repManager.savePlayerEntry(targetPlayerEntry, amount);

        // refresh targetPlayerEntry
        targetPlayerEntry = repManager.getPlayerEntry(targetUUID);

        sender.sendMessage(prefix + ChatColor.GREEN + "Applied staff reputation modifier: " + amount + " to " + targetName);
        sender.sendMessage(ChatColor.YELLOW 
            + " -----Last AVG: " + String.format("%.1f", targetPlayerEntry.getRepAverageLast()) + "  -----Current AVG: " + String.format("%.1f", targetPlayerEntry.getRepAverage()) + "\n"
            + "Last Shown AVG: " + String.format("%.1f", targetPlayerEntry.getRepShownLast())   + " Current Shown AVG: " + String.format("%.1f", targetPlayerEntry.getRepShown())
        );
        return true; 
    }

    private boolean handleShowCommand(CommandSender sender, String[] args) {
        // /repadmin show <player>
        if (args.length == 2) {
            String targetName = args[1];
            PlayerEntry targetPlayerEntry = repManager.getPlayerEntry(repManager.getPlayerUUID(targetName));
            if (targetPlayerEntry != null) {
                sender.sendMessage(ChatColor.DARK_RED + "--- " + ChatColor.RED + targetPlayerEntry.getPlayerUsername() + " Rep Data" + ChatColor.DARK_RED + " ---\n"
                    + ChatColor.GRAY + " -----Last AVG: " + ChatColor.WHITE + String.format("%.1f", targetPlayerEntry.getRepAverageLast()) + ChatColor.GRAY + "  -----Current AVG: " + ChatColor.WHITE + String.format("%.1f", targetPlayerEntry.getRepAverage()) + "\n"
                    + ChatColor.GRAY + "Last Shown AVG: " + ChatColor.WHITE + String.format("%.1f", targetPlayerEntry.getRepShownLast()) + ChatColor.GRAY +   " Current Shown AVG: " + ChatColor.WHITE + String.format("%.1f", targetPlayerEntry.getRepShown()) + "\n"
                    + ChatColor.GRAY + "Staff Modifier: " + ChatColor.WHITE + targetPlayerEntry.getRepStaffModifier() + ChatColor.GRAY + " Rep Count: " + ChatColor.WHITE + targetPlayerEntry.getRepCount()+ "\n"
                    + ChatColor.GRAY + "Initiators (have set score on " + targetPlayerEntry.getPlayerUsername() + "): " 
                );
                for (String n : repManager.getRepInitiators(targetPlayerEntry.getPlayerUUID())) {
                    int n_score = repManager.getRepEntry(repManager.getPlayerUUID(n), repManager.getPlayerUUID(targetName)).getRep();
                    sender.sendMessage(ChatColor.GRAY + "  - " + ChatColor.DARK_GRAY + n + ChatColor.GRAY + ": " + n_score);
                }
                sender.sendMessage(ChatColor.GRAY + "Receivers (have been scored by " + targetPlayerEntry.getPlayerUsername() + "): ");
                for (String n : repManager.getRepReceivers(targetPlayerEntry.getPlayerUUID())) {
                    int n_score = repManager.getRepEntry(repManager.getPlayerUUID(targetName), repManager.getPlayerUUID(n)).getRep();
                    sender.sendMessage(ChatColor.GRAY + "  - " + ChatColor.DARK_GRAY + n + ChatColor.GRAY + ": " + n_score);
                }
                return true;
            } else {
                sender.sendMessage(prefix + ChatColor.RED + "Could not find target player!");
                return false;
            }
        } 

        // /repadmin show <initiator> <receiver>
        else if (args.length == 3) {
            String initiator = args[1];
            String receiver = args[2];
            RepEntry targetRepEntry = repManager.getRepEntry(repManager.getPlayerUUID(initiator), repManager.getPlayerUUID(receiver));
            if (targetRepEntry == null) {
                sender.sendMessage(prefix + ChatColor.RED + "RepEntry not found.");
                return false;
            }
            sender.sendMessage(prefix + ChatColor.GRAY + initiator + " -> " + receiver + ": " + targetRepEntry.getRep());
            return true;

        } else {
            sender.sendMessage(prefix + ChatColor.GRAY + "Usage 1: /repadmin show <player>");
            sender.sendMessage(prefix + ChatColor.GRAY + "Usage 2: /repadmin show <initiator> <receiver>");
            return false;
        }

    }

    private boolean handleResetCommand(CommandSender sender, String[] args) {
        // /repadmin reset <player>
        if (args.length == 2) {
            String targetName = args[1];
            repManager.resetPlayerEntry(repManager.getPlayerUUID(targetName));
            sender.sendMessage(prefix + ChatColor.GREEN + "PlayerEntry should be deleted: " + targetName + "\n"
                + ChatColor.GREEN + "All RepEntries created by player should be deleted: " + targetName + "\n"
                + ChatColor.GREEN + "All RepEntries created on player should be deleted: " + targetName
            );
            return true;
        } else {
            sender.sendMessage(prefix + ChatColor.GRAY + "Usage: /repadmin reset <player>");
            return false;
        }
    }

    private boolean handleDeleteCommand(CommandSender sender, String[] args) {
        // /repadmin delete <initiator> <receiver>
        if (args.length == 3) {
            String initiator = args[1];
            String receiver = args[2];
            repManager.deleteRepEntry(repManager.getPlayerUUID(initiator), repManager.getPlayerUUID(receiver));
            sender.sendMessage(prefix + ChatColor.GREEN + "RepEntry should be deleted: " + initiator + " -> " + receiver);
            return true;
        } else {
            sender.sendMessage(prefix + ChatColor.GRAY + "Usage: /repadmin delete <initiator> <receiver>");
            return false;
        }
    }

    private boolean handleDeleteRepsByCommand(CommandSender sender, String[] args) {
        // /repadmin deleterepsby <initiator>
        if (args.length != 2) {
            sender.sendMessage(prefix + ChatColor.GRAY + "Usage: /repadmin deleteRepsBy <initiator>");
            return false;
        }
        String initiator = args[1];
        repManager.deleteRepEntriesByInitiator(repManager.getPlayerUUID(initiator));
        sender.sendMessage(prefix + ChatColor.GREEN + "All RepEntries created by player should be deleted: " + initiator);
        return true;
    }

    private boolean handleDeleteRepsOnCommand(CommandSender sender, String[] args) {
        // /repadmin deleterepson <receiver>
        if (args.length != 2) {
            sender.sendMessage(prefix + ChatColor.GRAY + "Usage: /repadmin deleteRepsOn <receiver>");
            return false;
        }
        String receiver = args[1];
        repManager.deleteRepEntriesByReceiver(repManager.getPlayerUUID(receiver));
        sender.sendMessage(prefix + ChatColor.GREEN + "All RepEntries created on player should be deleted: " + receiver);
        return true;
    }

    private boolean handleReloadCommand(CommandSender sender, String[] args) {
        // /repadmin reloadData
        if (args.length != 1) {
            sender.sendMessage(prefix + ChatColor.GRAY + "Usage: /repadmin reloadData");
            return false;
        }
        try {
            repManager.reload();
            sender.sendMessage(prefix + ChatColor.GREEN + "You successfully reloaded the databases!");
            return true;
        } catch (Exception e){
            System.out.println("Error reloading databases!");
            sender.sendMessage(prefix + ChatColor.RED + "Error reloading databases!");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            list.clear();
            for (String n : subCommands) {
                if (n!=null && n.startsWith(args[0])) {
                    list.add(n);
                }
            }
        }
        if (args.length == 2) {
            list.clear();
            if ( !args[0].equalsIgnoreCase("reloadData") ) {
                for (String n : repManager.username_map.keySet()) {
                    if (n!=null && n.startsWith(args[1])) {
                        list.add(n);
                    }
                }
            }
        }
        if (args.length == 3) {
            list.clear();
            if ( (args[0].equalsIgnoreCase("delete"))
              || (args[0].equalsIgnoreCase("show")) ) {
                for (String n : repManager.username_map.keySet()) {
                    if (n!=null && n.startsWith(args[2])) {
                        list.add(n);
                    }
                }
            }
        }
        if (!sender.hasPermission("rep64.admin")) list.clear();
        return list;
    }
}