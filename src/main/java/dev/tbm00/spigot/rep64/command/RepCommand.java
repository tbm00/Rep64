package dev.tbm00.spigot.rep64.command;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import dev.tbm00.spigot.rep64.RepManager;
import dev.tbm00.spigot.rep64.model.PlayerEntry;
import dev.tbm00.spigot.rep64.model.RepEntry;

public class RepCommand implements TabExecutor {
    private final JavaPlugin javaPlugin;
    private final RepManager repManager;
    private final String[] subCommands = new String[]{"help"};
    private final String[] subSubCommands = new String[]{"<#>", "?", "unset"};
    private final String prefix = ChatColor.DARK_GRAY + "[" + ChatColor.WHITE + "Rep" + ChatColor.DARK_GRAY + "] " + ChatColor.RESET;
    private final double defaultRep;
    private final double maxRep;
    private final double minRep;
    private final int maxRepInt;
    private final int minRepInt;

    public RepCommand(JavaPlugin javaPlugin, RepManager repManager) {
        this.javaPlugin = javaPlugin;
        this.repManager = repManager;
        this.defaultRep = javaPlugin.getConfig().getInt("repScoring.defaultRep");
        this.maxRep = javaPlugin.getConfig().getInt("repScoring.maxRep");
        this.minRep = javaPlugin.getConfig().getInt("repScoring.minRep");
        this.maxRepInt = javaPlugin.getConfig().getInt("repScoring.maxRep");
        this.minRepInt = javaPlugin.getConfig().getInt("repScoring.minRep");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(prefix + ChatColor.RED + "This command can only be run by a player!");
            return false;
        }

        Bukkit.getScheduler().runTaskAsynchronously(javaPlugin, () -> {
            // /rep
            if (args.length == 0) {
                handleSelfCommand(sender, args);
                return;
            }

            String subCommand = args[0].toLowerCase();

            switch (subCommand) {
                case "help":
                    handleHelpCommand(sender, args);
                    return;
                case "list":
                    handleListCommand(sender, args);
                    return;
                default:
                    break;
            }

            // /rep <player>
            if (args.length == 1) {
                handleGetCommand(sender, args);
                return;
            }

            // /rep <player> <#>/unset/?
            if (args.length == 2) {
                handleScoreCommand(sender, args);
                return;
            }
        });

        return true;
    }
    private boolean handleSelfCommand(CommandSender sender, String[] arg) {
        if (!sender.hasPermission("rep64.show")) {
            sender.sendMessage(prefix + ChatColor.RED + "No permission!");
            return false;
        }
        
        Player targetPlayer = (Player) sender;
        PlayerEntry targetPlayerEntry = repManager.getPlayerEntry(targetPlayer.getUniqueId().toString());
        if (targetPlayerEntry != null) {
            double repShown = targetPlayerEntry.getRepShown();
            targetPlayer.sendMessage(prefix + ChatColor.GRAY + "Your Reputation: " + ChatColor.LIGHT_PURPLE + String.format("%.1f", repShown) 
                + ChatColor.DARK_GRAY + " (avg of " + targetPlayerEntry.getRepCount() + " entries)");
            return true;
        } else {
            targetPlayer.sendMessage(prefix + ChatColor.RED + "Could not find your player entry... creating a new one!");
            targetPlayerEntry = new PlayerEntry(targetPlayer.getUniqueId().toString(), targetPlayer.getName(), defaultRep, 0.0, 0, defaultRep, 0.0, 0, new Date(), new Date());
            targetPlayerEntry.setLastLogin(new Date());
            repManager.savePlayerEntry(targetPlayerEntry);
            repManager.loadPlayerCache(targetPlayer.getName());
            return false;
        }
    }

    private boolean handleHelpCommand(CommandSender sender, String[] args) {
        sender.sendMessage(ChatColor.DARK_PURPLE + "--- " + ChatColor.LIGHT_PURPLE + "Rep64 Commands" + ChatColor.DARK_PURPLE + " ---\n"
        + ChatColor.WHITE + "/rep" + ChatColor.GRAY + " Display your average reputation\n"
        + ChatColor.WHITE + "/rep help" + ChatColor.GRAY + " Display this command list\n"
        + ChatColor.WHITE + "/rep <player>" + ChatColor.GRAY + " Display <player>'s average reputation\n"
        + ChatColor.WHITE + "/rep <player> ?" + ChatColor.GRAY + " Display the rep score you gave <player>\n"
        + ChatColor.WHITE + "/rep <player> <#>" + ChatColor.GRAY + " Give <player> a rep score\n"
        + ChatColor.WHITE + "/rep <player> unset" + ChatColor.GRAY + " Delete your rep score on <player>"
        );
        if (sender.hasPermission("rep64.list")) {
            sender.sendMessage(ChatColor.WHITE + "/rep list" + ChatColor.GRAY + " Display your rep lists");
        }
        if (sender.hasPermission("rep64.list.others")) {
            sender.sendMessage(ChatColor.WHITE + "/rep list <player>" + ChatColor.GRAY + " Display player's rep lists");
        }
        return true;
    }

    private boolean handleListCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("rep64.list")) {
            sender.sendMessage(prefix + ChatColor.RED + "No permission!");
            return false;
        }
        
        if (args.length != 1 && args.length != 2) {
            sender.sendMessage(prefix + ChatColor.GRAY + "Usage 1: /rep list");
            sender.sendMessage(prefix + ChatColor.GRAY + "Usage 2: /rep list <player>");
            return false;
        }

        String targetName = null;
        PlayerEntry targetPlayerEntry = null;

        // /rep list
        if (args.length == 1) {
            Player targetPlayer = (Player) sender;
            targetPlayerEntry = repManager.getPlayerEntry(targetPlayer.getUniqueId().toString());
            if (targetPlayerEntry == null) {
                sender.sendMessage(prefix + ChatColor.RED + "Could not find your player entry... creating a new one!");
                targetPlayerEntry = new PlayerEntry(targetPlayer.getUniqueId().toString(), targetPlayer.getName(), defaultRep, 0.0, 0, defaultRep, 0.0, 0, new Date(), new Date());
                targetPlayerEntry.setLastLogin(new Date());
                repManager.savePlayerEntry(targetPlayerEntry);
                repManager.loadPlayerCache(targetPlayer.getName());
                return false;
            }
        } 
        // /rep list <player>
        else if (args.length == 2) {
            if (!sender.hasPermission("rep64.list.others")) {
                sender.sendMessage(prefix + ChatColor.RED + "No permission!");
                return false;
            }
            targetName = args[1];
            targetPlayerEntry = repManager.getPlayerEntry(repManager.getPlayerUUID(targetName));
            if (targetPlayerEntry == null) {
                sender.sendMessage(prefix + ChatColor.RED + "Could not find target player's rep lists!");
                return false;
            }
        }

        if (targetPlayerEntry == null) {
            sender.sendMessage(prefix + ChatColor.RED + "Could not find rep lists!");
            return false;
        }
        
        sender.sendMessage(ChatColor.DARK_PURPLE + "--- " + ChatColor.LIGHT_PURPLE + targetPlayerEntry.getPlayerUsername() + " Rep Lists" + ChatColor.DARK_PURPLE + " ---\n"
            + ChatColor.GRAY + " Current Average: " + ChatColor.WHITE + String.format("%.1f", targetPlayerEntry.getRepShown())
            + ChatColor.GRAY + " (avg of " + targetPlayerEntry.getRepCount() + " entries)" + "\n"
            
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
    }

    private boolean handleGetCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("rep64.show.others")) {
            sender.sendMessage(prefix + ChatColor.RED + "No permission!");
            return false;
        }

        Player player = (Player) sender;
        String targetName = args[0];
        PlayerEntry targetPlayerEntry = repManager.getPlayerEntry(repManager.getPlayerUUID(targetName));
        if (targetPlayerEntry != null) {
            double targetRepShown = targetPlayerEntry.getRepShown();
            player.sendMessage(prefix + ChatColor.GRAY + targetName + "'s Reputation: " + ChatColor.LIGHT_PURPLE + String.format("%.1f", targetRepShown) 
                + ChatColor.DARK_GRAY + " (avg of " + targetPlayerEntry.getRepCount() + " entries)");
            return true;
        } else {
            player.sendMessage(prefix + ChatColor.RED + "Could not find target player's reputation!");
            return false;
        }
    }

    private boolean handleScoreCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("rep64.set")) {
            sender.sendMessage(prefix + ChatColor.RED + "No permission!");
            return false;
        }

        Player initiator = (Player) sender;
        String targetName = args[0];
        String action = args[1];

        String receiverUUID = repManager.getPlayerUUID(targetName);

        
        if ((!sender.hasPermission("rep64.set.self")) && (initiator.getUniqueId().toString().equals(receiverUUID)))  {
            sender.sendMessage(prefix + ChatColor.RED + "You cannot set a rep score on yourself!");
            return false;
        }
        

        // /rep <player> unset
        PlayerEntry targetPlayerEntry = repManager.getPlayerEntry(receiverUUID);
        if (action.equalsIgnoreCase("unset")) {
            if (targetPlayerEntry != null) {
                String initiatorUUID = initiator.getUniqueId().toString();

                RepEntry targetRepEntry = repManager.getRepEntry(initiatorUUID, receiverUUID);
                if (targetRepEntry == null) {
                    sender.sendMessage(prefix + ChatColor.RED + "You have not set a rep score on " + targetName + "!");
                    return true;
                }

                repManager.deleteRepEntry(initiatorUUID, receiverUUID);

                // get fresh target player entry
                targetPlayerEntry = repManager.getPlayerEntry(receiverUUID);
                sender.sendMessage(prefix + ChatColor.GREEN + "You have removed your rep score on " + targetName + "!");
                sender.sendMessage(ChatColor.YELLOW + "Last AVG: " + String.format("%.1f", targetPlayerEntry.getRepShownLast())
                                    + ", Current AVG: " + String.format("%.1f", targetPlayerEntry.getRepShown()));
                return true;
            } else {
                sender.sendMessage(prefix + ChatColor.RED + "Could not find target player!");
                return false;
            }
        } 

        // /rep <player> ?
        else if (action.equalsIgnoreCase("?")) {
            if (targetPlayerEntry != null) {
                String initiatorUUID = initiator.getUniqueId().toString();
                RepEntry targetRepEntry = repManager.getRepEntry(initiatorUUID, receiverUUID);
                if (targetRepEntry == null) {
                    sender.sendMessage(prefix + ChatColor.RED + "You have not set a rep score on " + targetName + "!");
                    return true;
                }
                int targetRep = targetRepEntry.getRep();
                sender.sendMessage(prefix + ChatColor.GREEN + "You previously gave " + targetName + " a rep score of " + targetRep + "!");
                return true;
            } else {
                sender.sendMessage(prefix + ChatColor.RED + "Could not find target player!");
                return false;
            }
        } 
        
        // /rep <player> <#>
        else {
            try {
                int rep = Integer.parseInt(args[1]);
                if (rep < minRep || rep > maxRep) {
                    sender.sendMessage(prefix + ChatColor.RED + "The rep score must be an integer between " + minRepInt + " and " + maxRepInt + "!");
                    return false;
                }

                targetPlayerEntry = repManager.getPlayerEntry(receiverUUID);
                if (targetPlayerEntry == null) {
                    sender.sendMessage(prefix + ChatColor.RED + "Could not find target player!");
                    return false;
                }

                // get prior/current rep entry
                RepEntry targetRepEntry = repManager.getRepEntry(initiator.getUniqueId().toString(), receiverUUID);
                if (targetRepEntry==null) {
                    targetRepEntry = new RepEntry(initiator.getUniqueId().toString(), receiverUUID, rep);
                } else {
                    targetRepEntry.setRep(rep);
                }

                // calculate and save/create new rep entry in to databases (sql and cache)
                repManager.saveRepEntry(targetRepEntry);

                // refresh targetPlayerEntry
                targetPlayerEntry = repManager.getPlayerEntry(receiverUUID);
                
                sender.sendMessage(prefix + ChatColor.GREEN + "You gave " + repManager.getPlayerEntry(targetRepEntry.getReceiverUUID()).getPlayerUsername() 
                    + " a rep score of " + targetRepEntry.getRep() + "!");
                sender.sendMessage(ChatColor.YELLOW + "Last AVG: " + String.format("%.1f", targetPlayerEntry.getRepShownLast())
                                    + ", Current AVG: " + String.format("%.1f", targetPlayerEntry.getRepShown()));
                return true;
            } catch (NumberFormatException e) {
                sender.sendMessage(prefix + ChatColor.GRAY + "Usage: /rep <player> <?, #, unset>");
                return false;
            }
        }
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
            if (sender.hasPermission("rep64.list")) {
                String n = "list";
                if (n.startsWith(args[0])) {
                    list.add(n);
                }
            }
            for (String n : repManager.username_map.keySet()) {
                if (n!=null && n.startsWith(args[0])) {
                    list.add(n);
                }
            }
        }
        if (args.length == 2) {
            list.clear();
            if ( !args[0].equalsIgnoreCase("help") && !args[0].equalsIgnoreCase("list") ) {
                for (String n : subSubCommands) {
                    if (n!=null && n.startsWith(args[1])) {
                        list.add(n);
                    }
                }
            }
            if (sender.hasPermission("rep64.list.others") && !args[0].equalsIgnoreCase("help")) {
                for (String n : repManager.username_map.keySet()) {
                    if (n!=null && n.startsWith(args[1])) {
                        list.add(n);
                    }
                }
            }
        }
        return list;
    }
}
