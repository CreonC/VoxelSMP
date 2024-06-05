package me.creonc.voxelsmp;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import me.creonc.voxelsmp.commands.Settings;

import java.util.Objects;

public final class VoxelSMP extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getLogger().info("Starting VoxelSMP core");
        getConfig().options().copyDefaults(true);
        this.saveConfig();
        Settings settings = new Settings();

        try {
            this.getCommand("settings").setExecutor(settings);
        }
        catch (NullPointerException e) {
            Bukkit.getLogger().severe("Failed to initialize VoxelSMP commands. Reason: " + e.getCause());
        }


    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Bukkit.getLogger().info("Stopping VoxelSMP core");
        saveConfig();
    }
}
