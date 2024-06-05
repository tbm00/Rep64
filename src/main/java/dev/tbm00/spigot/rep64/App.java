package dev.tbm00.spigot.rep64;

import org.bukkit.plugin.java.JavaPlugin;

public class App extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("Hello, Console!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Goodbye, Console!");
    }

}