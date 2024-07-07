package dev.tbm00.spigot.rep64.data;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import dev.tbm00.spigot.rep64.RepManager;

public class CacheManager {
    private final JavaPlugin plugin;
    private final RepManager repManager;

    public CacheManager(JavaPlugin plugin, RepManager repManager, FileConfiguration fileConfig) {
        this.plugin = plugin;
        this.repManager = repManager;
        boolean enabled = fileConfig.getBoolean("autoCacheReloader.enabled");
        int ticksBetween = fileConfig.getInt("autoCacheReloader.ticksBetween");
        startScheduler(enabled, ticksBetween);
    }

    private void startScheduler(boolean enabled, int ticksBetween) {
        if (enabled == false) return;
        new BukkitRunnable() {
            @Override
            public void run() {
                System.out.println("[auto] Clearing and reloading caches...");
                try {
                    repManager.reloadCache();
                } catch (Exception e) {
                    System.out.println("[auto] Exception: Could not reload cache...");
                    e.printStackTrace();
                }
                System.out.println("[auto] Caches reloaded!");
            }
        }.runTaskTimer(plugin, 0L, ticksBetween);
    }
}