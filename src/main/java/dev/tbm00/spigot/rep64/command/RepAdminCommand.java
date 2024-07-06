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

public class RepAdminCommand implements TabExecutor {
    private RepManager repManager;
    private String[] subCommands = new String[]{"mod", "show", "deleterepsby", "deleterepson", "delete", "reload"};
    private String[] subSubSubCommands = new String[]{"show", "<#>"};
    private final String prefix = ChatColor.DARK_GRAY + "[" + ChatColor.WHITE + "Rep" + ChatColor.DARK_GRAY + "] " + ChatColor.RESET;

    public RepAdminCommand(RepManager repManager) {
        this.repManager = repManager;
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
                sender.sendMessage(prefix + ChatColor.RED + "This command can only be run by a player.");
                return true;
            }
            Player initiator = (Player) sender;
            initiator.sendMessage(ChatColor.DARK_RED + "--- " + ChatColor.RED + "Rep64 Admin Commands" + ChatColor.DARK_RED + " ---\n"
                + ChatColor.WHITE + "/repadmin" + ChatColor.GRAY + " Display this command list\n"
                + ChatColor.WHITE + "/repadmin mod <player> #" + ChatColor.GRAY + " Give <player> a rep modifier (added to avg)\n"
                + ChatColor.WHITE + "/repadmin show <intiator> <receiver>" + ChatColor.GRAY + " Display a specific RepEntry\n"
                + ChatColor.WHITE + "/repadmin delete <intiator> <receiver>" + ChatColor.GRAY + " Delete a specific RepEntry\n"
                + ChatColor.WHITE + "/repadmin deleterepsby <intiator>" + ChatColor.GRAY + " Delete RepEntries created by <intiator>\n"
                + ChatColor.WHITE + "/repadmin deleterepson <receiver>" + ChatColor.GRAY + " Delete RepEntries created on <receiver>\n"
                + ChatColor.WHITE + "/repadmin delete <player>" + ChatColor.GRAY + " (BIG!) Delete PlayerEntry & all associated RepEntries\n"
                + ChatColor.WHITE + "/repadmin reload" + ChatColor.GRAY + " Reload MySQL database and refresh plugin's caches\n"
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
            case "reload":
                return handleReloadCommand(sender, args);
            default:
                sender.sendMessage(prefix + ChatColor.RED + "Unknown subcommand!");
                return false;
        }
    }

    private boolean handleModCommand(CommandSender sender, String[] args) {
        if (args.length != 3) {
            sender.sendMessage(prefix + ChatColor.RED + "Usage: /repadmin mod <player> show/<amount>");
            return false;
        }
        String targetName = args[1];

        if (args[2].equalsIgnoreCase("show")) {
            PlayerEntry targetEntry = repManager.getPlayerEntry(repManager.getPlayerUUID(targetName));
            if (targetEntry != null) {
                sender.sendMessage(ChatColor.DARK_RED + "--- " + ChatColor.RED + targetEntry.getPlayerUsername() + " Rep Info" + ChatColor.DARK_RED + " ---\n"
                + ChatColor.GRAY + "----- Last AVG: " + ChatColor.WHITE + targetEntry.getRepAverageLast() + ChatColor.GRAY + " ----- AVG: " + ChatColor.WHITE + targetEntry.getRepAverage() + "\n"
                + ChatColor.GRAY + "Last Shown AVG: " + ChatColor.WHITE + targetEntry.getRepShownLast() + ChatColor.GRAY + " Shown AVG: " + ChatColor.WHITE + targetEntry.getRepShown() + "\n"
                + ChatColor.GRAY + "Staff Modifier: " + ChatColor.WHITE + targetEntry.getRepStaffModifier() + ChatColor.GRAY + " Rep Count: " + ChatColor.WHITE + targetEntry.getRepCount()+ "\n"
                + ChatColor.GRAY + "Initiators: " 
                );
                for (String n : repManager.getRepInitiators(targetEntry.getPlayerUUID())) {
                    sender.sendMessage(ChatColor.DARK_GRAY + n + ChatColor.GRAY + ", ");
                }
                return true;
            } else {
                sender.sendMessage(prefix + ChatColor.RED + "Could not find target player!");
                return true;
            }
        } else {
            int amount;
            try {
                amount = Integer.parseInt(args[2]);
                if (amount <= -10 || amount >= 10) {
                    sender.sendMessage(prefix + ChatColor.RED + "Invalid amount. It must be between -10 and 10!");
                    return false;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(prefix + ChatColor.RED + "Invalid amount. It must be an integer!");
                return false;
            }

            PlayerEntry targetPlayerEntry = repManager.getPlayerEntry(repManager.getPlayerUUID(targetName));
            if (targetPlayerEntry == null) {
                sender.sendMessage(prefix + ChatColor.RED + "Player not found.");
                return false;
            }

            targetPlayerEntry.setRepStaffModifier(amount);
            repManager.savePlayerEntry(targetPlayerEntry);

            sender.sendMessage(prefix + ChatColor.GREEN + "Applied rep modifier: " + amount + " to " + targetName);
            sender.sendMessage(prefix + ChatColor.GRAY + targetPlayerEntry.getPlayerUsername() + "'s Rep: " 
                            + ChatColor.RED + String.format("%.1f", targetPlayerEntry.getRepShownLast())
                            + ChatColor.DARK_GRAY + " (avg of " + targetPlayerEntry.getRepCount() + " entries)");
            sender.sendMessage(prefix + ChatColor.GRAY + targetPlayerEntry.getPlayerUsername() + "'s Rep: " 
                            + ChatColor.LIGHT_PURPLE + String.format("%.1f", targetPlayerEntry.getRepShown()) 
                            + ChatColor.DARK_GRAY + " (avg of " + targetPlayerEntry.getRepCount() + " entries)");
            return true; 
        }
    }

    private boolean handleShowCommand(CommandSender sender, String[] args) {
        if (args.length != 3) {
            sender.sendMessage(prefix + ChatColor.RED + "Usage: /repadmin show <initiator> <receiver>");
            return false;
        }
        String initiator = args[1];
        String receiver = args[2];
        RepEntry targetRepEntry = repManager.getRepEntry(repManager.getPlayerUUID(initiator), repManager.getPlayerUUID(receiver));
        if (targetRepEntry == null) {
            sender.sendMessage(prefix + ChatColor.RED + "Reputation entry not found.");
            return false;
        }
        sender.sendMessage(prefix + ChatColor.GRAY + initiator + " -> " + receiver + ": " + targetRepEntry.getRep());
        return true;
    }

    private boolean handleDeleteCommand(CommandSender sender, String[] args) {
        if (args.length == 2) {
            String targetName = args[1];
            repManager.deletePlayerEntry(repManager.getPlayerUUID(targetName));
            sender.sendMessage(prefix + ChatColor.GREEN + "Player entry should be deleted: " + targetName);
            sender.sendMessage(prefix + ChatColor.GREEN + "All rep entries created by player should be deleted: " + targetName);
            sender.sendMessage(prefix + ChatColor.GREEN + "All rep entries created on player should be deleted: " + targetName);
            return true;
        } else if (args.length == 3) {
            String initiator = args[1];
            String receiver = args[2];
            repManager.deleteRepEntry(repManager.getPlayerUUID(initiator), repManager.getPlayerUUID(receiver));
            sender.sendMessage(prefix + ChatColor.GREEN + "Rep entry should be deleted: " + initiator + " -> " + receiver);
            return true;
        } else {
            sender.sendMessage(prefix + ChatColor.RED + "Usage: /repadmin delete <player> or /repadmin delete <initiator> <receiver>");
            return false;
        }
    }

    private boolean handleDeleteRepsByCommand(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(prefix + ChatColor.RED + "Usage: /repadmin deleterepsby <initiator>");
            return false;
        }
        String initiator = args[1];
        repManager.deleteRepEntriesByInitiator(repManager.getPlayerUUID(initiator));
        sender.sendMessage(prefix + ChatColor.GREEN + "All rep entries created by player should be deleted: " + initiator);
        return true;
    }

    private boolean handleDeleteRepsOnCommand(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(prefix + ChatColor.RED + "Usage: /repadmin deleterepson <receiver>");
            return false;
        }
        String receiver = args[1];
        repManager.deleteRepEntriesByReceiver(repManager.getPlayerUUID(receiver));
        sender.sendMessage(prefix + ChatColor.GREEN + "All rep entries created on player should be deleted: " + receiver);
        return true;
    }

    private boolean handleReloadCommand(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(prefix + ChatColor.RED + "Usage: /repadmin reload");
            return false;
        }
        try {
            repManager.reload();
            sender.sendMessage(prefix + ChatColor.GREEN + "You successfully reloaded the database!");
            return true;
        } catch (Exception e){
            System.out.println("Error reloading database!");
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
            for (String n : repManager.username_map.keySet()) {
                if (n!=null && n.startsWith(args[1])) {
                    list.add(n);
                }
            }
        }
        if (args.length == 3) {
            list.clear();
            if ( (!args[0].equalsIgnoreCase("show"))
              && (!args[0].equalsIgnoreCase("mod")) ) {
                for (String n : repManager.username_map.keySet()) {
                    if (n!=null && n.startsWith(args[2])) {
                        list.add(n);
                    }
                }
            }
            if ( args[0].equalsIgnoreCase("mod") ) {
                for (String n : subSubSubCommands) {
                    if (n!=null && n.startsWith(args[2])) {
                        list.add(n);
                    }
                }
            }
        }
        return list;
    }
}