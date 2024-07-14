package dev.tbm00.spigot.rep64.data;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import dev.tbm00.spigot.rep64.RepManager;

public class CacheManager {
    private final JavaPlugin javaPlugin;
    private final RepManager repManager;

    public CacheManager(JavaPlugin javaPlugin, RepManager repManager) {
        this.javaPlugin = javaPlugin;
        this.repManager = repManager;
        boolean enabled = javaPlugin.getConfig().getBoolean("autoCacheReloader.enabled");
        int ticksBetween = javaPlugin.getConfig().getInt("autoCacheReloader.ticksBetween");
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
                    System.out.println("Exception: autoCacheReloader could not reload cache!");
                    e.printStackTrace();
                }
                System.out.println("[auto] Caches reloaded!");
            }
        }.runTaskTimer(javaPlugin, 0L, ticksBetween);
        System.out.println("Started autoCacheReloader!");
    }
}