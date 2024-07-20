package dev.tbm00.spigot.rep64.expansion;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import dev.tbm00.spigot.rep64.RepManager;
import dev.tbm00.spigot.rep64.model.PlayerEntry;

public class Rep64PAPI extends PlaceholderExpansion {
    private final RepManager repManager;
    private final int defaultRep;

    public Rep64PAPI(JavaPlugin javaPlugin, RepManager repManager) {
        this.repManager = repManager;
        this.defaultRep = javaPlugin.getConfig().getInt("repScoring.defaultRep");
    }

    @Override
    public @NotNull String getIdentifier() {
        return "rep64";
    }

    @Override
    public @NotNull String getAuthor() {
        return "tbm00";
    }

    @Override
    public @NotNull String getVersion() {
        return "0.4";
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) {
            return String.valueOf(defaultRep);
        }

        PlayerEntry playerEntry = repManager.getPlayerEntry(player.getUniqueId().toString());
        if (playerEntry == null) {
            return String.valueOf(defaultRep);
        }

        switch (identifier) {
            case "rep_shown":
                return String.format("%.1f", playerEntry.getRepShown());
            case "rep_shown_int":
                return String.valueOf((int) Math.round(playerEntry.getRepShown()));
            default:
                return null;
        }
    }
}