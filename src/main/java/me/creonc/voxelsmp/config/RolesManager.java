package me.creonc.voxelsmp.config;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class RolesManager {
        private final Plugin plugin;
    private final File roleFile;
    private FileConfiguration roleConfig;

    public RolesManager(Plugin plugin) {
        this.plugin = plugin;
        this.roleFile = new File(plugin.getDataFolder(), "roles.yml");
        loadRoleConfig();
    }

    private void loadRoleConfig() {
        if (!roleFile.exists()) {
            plugin.saveResource("roles.yml", false);
        }
        roleConfig = YamlConfiguration.loadConfiguration(roleFile);
    }

    public void saveRoleConfig() {
        try {
            roleConfig.save(roleFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setPlayerRole(UUID playerUUID, String role) {
        Set<String> roles = getPlayerRoles(playerUUID);
        roles.add(role);
        roleConfig.set(playerUUID.toString(), roles.toArray(new String[0]));
        saveRoleConfig();
    }

    public void removePlayerRole(UUID playerUUID, String role) {
        Set<String> roles = getPlayerRoles(playerUUID);
        roles.remove(role);
        roleConfig.set(playerUUID.toString(), roles.toArray(new String[0]));
        saveRoleConfig();
    }

    public Set<String> getPlayerRoles(UUID playerUUID) {
        return new HashSet<>(roleConfig.getStringList(playerUUID.toString()));
    }

    public boolean hasRole(UUID playerUUID, String role) {
        return getPlayerRoles(playerUUID).contains(role);
    }
}
