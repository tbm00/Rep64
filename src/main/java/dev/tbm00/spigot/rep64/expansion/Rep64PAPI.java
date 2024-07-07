package dev.tbm00.spigot.rep64.expansion;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import dev.tbm00.spigot.rep64.RepManager;
import dev.tbm00.spigot.rep64.model.PlayerEntry;

public class Rep64PAPI extends PlaceholderExpansion {

    private final RepManager repManager;

    public Rep64PAPI(RepManager repManager) {
        this.repManager = repManager;
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
        return "0.3";
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) {
            return "5";
        }

        PlayerEntry playerEntry = repManager.getPlayerEntry(player.getUniqueId().toString());
        if (playerEntry == null) {
            return "5";
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