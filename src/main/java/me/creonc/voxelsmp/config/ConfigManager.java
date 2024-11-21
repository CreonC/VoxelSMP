package me.creonc.voxelsmp.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class ConfigManager {
    private final JavaPlugin plugin;
    private File configFile;
    private FileConfiguration config;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        setupConfig();
    }

    private void setupConfig() {
        // Save default config if it doesn't exist
        plugin.saveDefaultConfig();

        // Initialize config file
        configFile = new File(plugin.getDataFolder(), "config.yml");
        reloadConfig();
    }

    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);

        // Set defaults if they don't exist
        config.addDefault("discord.token", "your-token-here");
        config.addDefault("discord.enabled", true);
        config.addDefault("discord.channel-id", 0L);
        config.addDefault("purgeday.baseurl", "http://localhost");
        config.addDefault("purgeday.port", 8080);
        config.options().copyDefaults(true);

        saveConfig();
    }

    public void saveConfig() {
        try {
            // Save changes asynchronously to prevent blocking main thread
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    config.save(configFile);
                } catch (IOException e) {
                    plugin.getLogger().severe("Could not save config.yml: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            plugin.getLogger().severe("Could not save config.yml: " + e.getMessage());
        }
    }

    // Getters
    public String getToken() {
        return config.getString("discord.token");
    }

    public boolean isEnabled() {
        return config.getBoolean("discord.enabled");
    }

    public long getChannelId() {
        return config.getLong("discord.channel-id");
    }

    // Setters
    public void setToken(String token) {
        config.set("discord.token", token);
        saveConfig();
    }

    public void setEnabled(boolean enabled) {
        config.set("discord.enabled", enabled);
        saveConfig();
    }

    public void setChannelId(long channelId) {
        config.set("discord.channel-id", channelId);
        saveConfig();
    }

    public String getPurgeBaseUrl() {
        return config.getString("purgeday.baseurl");
    }

    public int getPurgePort() {
        return config.getInt("purgeday.port");
    }
}