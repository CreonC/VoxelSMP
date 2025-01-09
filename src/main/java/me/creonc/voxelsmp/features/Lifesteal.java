package me.creonc.voxelsmp.features;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import me.creonc.voxelsmp.VoxelSMP;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
//json very good
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class Lifesteal implements Listener {
    private final VoxelSMP plugin;
    private static final double HEART_AMOUNT = 2.0; // 1 heart = 2 HP
    private static final double MIN_HEALTH = 2.0; // Minimum 1 heart
    private boolean enabled = true;  // Toggle for lifesteal behavior
    private final Set<String> bannedPlayers = new HashSet<>();
    private final File bannedPlayersFile;
    private final Gson gson = new Gson();

    public Lifesteal(VoxelSMP plugin) {
        this.plugin = plugin;
        this.bannedPlayersFile = new File(plugin.getDataFolder(), "banned-players.json");
        loadBannedPlayers();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        plugin.getLogger().info("[Lifesteal] Feature " + (enabled ? "enabled" : "disabled"));
    }

    public boolean isEnabled() {
        return enabled;
    }

    @EventHandler
    public void onPlayerDied(PlayerDeathEvent event) {
        if (!enabled) {
            plugin.getLogger().info("[Lifesteal] Death event ignored - feature disabled");
            return;
        }

        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        plugin.getLogger().info("[Lifesteal] Player death detected - Victim: " + victim.getName() +
                (killer != null ? ", Killer: " + killer.getName() : ", No killer"));

        // Reduce victim's max health
        reduceMaxHealth(victim);

        // If killed by another player, increase killer's max health
        if (killer != null) {
            plugin.getLogger().info("[Lifesteal] Killer detected: " + killer.getName());
            increaseMaxHealth(killer);
        }
    }

    private void reduceMaxHealth(Player player) {
        double currentMaxHealth = player.getAttribute(Attribute.MAX_HEALTH).getBaseValue();
        double newMaxHealth = Math.max(currentMaxHealth - HEART_AMOUNT, MIN_HEALTH);

        player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(newMaxHealth);

        plugin.getLogger().info("[Lifesteal] Reducing " + player.getName() + "'s health from " +
                (currentMaxHealth/2) + " hearts to " + (newMaxHealth/2) + " hearts");

        // TODO: Discord integration

        // Only eliminate if the new health would be LESS than MIN_HEALTH
        if (currentMaxHealth <= MIN_HEALTH) {
            plugin.getLogger().info("[Lifesteal] Player " + player.getName() + " has been eliminated!");
            Bukkit.broadcastMessage("§c" + player.getName() + " has been eliminated!");
            banPlayer(player.getName());
            player.kick(Component.text("You have been eliminated!"));
        }
    }

    private void increaseMaxHealth(Player player) {
        AttributeInstance attribute = player.getAttribute(Attribute.MAX_HEALTH);
        if (attribute != null) {
            double currentMax = attribute.getBaseValue();
            double newMax = currentMax + HEART_AMOUNT; // No maximum cap

            plugin.getLogger().info("[Lifesteal] Increasing " + player.getName() + "'s health from " +
                    (currentMax/2) + " hearts to " + (newMax/2) + " hearts");

            attribute.setBaseValue(newMax);

            player.sendMessage("§a+1 Heart! New max health: " + (newMax/2) + " hearts");
        }
    }

    private void loadBannedPlayers() {
        if (!bannedPlayersFile.exists()) {
            saveBannedPlayers();
            return;
        }
        
        try (Reader reader = new FileReader(bannedPlayersFile)) {
            JsonObject json = gson.fromJson(reader, JsonObject.class);
            JsonArray banned = json.getAsJsonArray("banned_players");
            banned.forEach(element -> bannedPlayers.add(element.getAsString()));
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to load banned players: " + e.getMessage());
        }
    }

    private void saveBannedPlayers() {
        JsonObject json = new JsonObject();
        JsonArray banned = new JsonArray();
        bannedPlayers.forEach(banned::add);
        json.add("banned_players", banned);
        
        try (Writer writer = new FileWriter(bannedPlayersFile)) {
            gson.toJson(json, writer);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save banned players: " + e.getMessage());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (bannedPlayers.contains(player.getName())) {
            player.kick(Component.text("You have been eliminated from the server!"));
        }
    }

    public void banPlayer(String playerName) {
        bannedPlayers.add(playerName);
        saveBannedPlayers();
    }

    public void revivePlayer(String playerName) {
        if (bannedPlayers.remove(playerName)) {
            saveBannedPlayers();
            Player player = Bukkit.getPlayer(playerName);
            if (player != null) {
                player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(20.0); // Reset to 10 hearts
                //TODO: Teleport revived player back to the reviver (or spawn if there's no reviver)
            }
        }
    }

    // Optional: Method to reset a player's health
    public void resetHealth(Player player) {
        AttributeInstance attribute = player.getAttribute(Attribute.MAX_HEALTH);
        if (attribute != null) {
            plugin.getLogger().info("[Lifesteal] Resetting " + player.getName() + "'s health to 10 hearts");
            attribute.setBaseValue(20.0); // Default max health (10 hearts)
            player.setHealth(20.0);
        }
    }
}