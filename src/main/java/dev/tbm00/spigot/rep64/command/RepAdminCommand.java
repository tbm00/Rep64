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
    private String[] subCommands = new String[]{"mod, show, delete, deleterepsby, deleterepson"};
    private final String prefix = ChatColor.DARK_GRAY + "[" + ChatColor.WHITE + "Rep" + ChatColor.DARK_GRAY + "] " + ChatColor.RESET;

    public RepAdminCommand(RepManager repManager) {
        this.repManager = repManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // /repadmin
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(prefix + ChatColor.RED + "This command can only be run by a player.");
                return true;
            }
            Player initiator = (Player) sender;
            initiator.sendMessage(ChatColor.DARK_RED + "--- " + ChatColor.RED + "&cRep64 Admin Commands" + ChatColor.DARK_RED + " ---\n"
                + ChatColor.WHITE + "/repadmin" + ChatColor.GRAY + " Display this command list\n"
                + ChatColor.WHITE + "/repadmin mod <player> #" + ChatColor.GRAY + " Give <player> a rep modifier (added to avg)\n"
                + ChatColor.WHITE + "/repadmin show <intiator> <receiver>" + ChatColor.GRAY + " Display specific rep entry\n"
                + ChatColor.WHITE + "/repadmin delete <player>" + ChatColor.GRAY + " Delete <player>'s entry & associated rep entries\n"
                + ChatColor.WHITE + "/repadmin delete <intiator> <receiver>" + ChatColor.GRAY + " Delete specific rep entry\n"
                + ChatColor.WHITE + "/repadmin deleterepsby <intiator>" + ChatColor.GRAY + " Delete reps created by <intiator>\n"
                + ChatColor.WHITE + "/repadmin deleterepson <receiver>" + ChatColor.GRAY + " Delete reps created on <receiver>\n"
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
            default:
                sender.sendMessage(prefix + ChatColor.RED + "Unknown subcommand!");
                return false;
        }
    }

    private boolean handleModCommand(CommandSender sender, String[] args) {
        if (args.length != 3) {
            sender.sendMessage(prefix + ChatColor.RED + "Usage: /repadmin mod <player> <amount>");
            return false;
        }
        String targetName = args[1];

        int amount;
        try {
            amount = Integer.parseInt(args[2]);
            if (amount < -10 || amount > 10) {
                sender.sendMessage(prefix + ChatColor.RED + "Invalid amount. It must be between -10 and 10.");
                return false;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(prefix + ChatColor.RED + "Invalid amount. It must be a number.");
            return false;
        }

        PlayerEntry targetPlayerEntry = repManager.getPlayerEntry(targetName);
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

    private boolean handleShowCommand(CommandSender sender, String[] args) {
        if (args.length != 3) {
            sender.sendMessage(prefix + ChatColor.RED + "Usage: /repadmin show <initiator> <receiver>");
            return false;
        }
        String initiator = args[1];
        String receiver = args[2];
        RepEntry targetRepEntry = repManager.getRepEntry(initiator, receiver);
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
            repManager.deletePlayerEntry(targetName);
            sender.sendMessage(prefix + ChatColor.GREEN + "Player entry should be deleted: " + targetName);
            sender.sendMessage(prefix + ChatColor.GREEN + "All rep entries created by player should be deleted: " + targetName);
            sender.sendMessage(prefix + ChatColor.GREEN + "All rep entries created on player should be deleted: " + targetName);
            return true;
        } else if (args.length == 3) {
            String initiator = args[1];
            String receiver = args[2];
            repManager.deleteRepEntry(initiator, receiver);
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
        repManager.deleteRepEntriesByInitiator(initiator);
        sender.sendMessage(prefix + ChatColor.GREEN + "All rep entries created by player should be deleted: " + initiator);
        return true;
    }

    private boolean handleDeleteRepsOnCommand(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(prefix + ChatColor.RED + "Usage: /repadmin deleterepson <receiver>");
            return false;
        }
        String receiver = args[1];
        repManager.deleteRepEntriesByReceiver(receiver);
        sender.sendMessage(prefix + ChatColor.GREEN + "All rep entries created on player should be deleted: " + receiver);
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
