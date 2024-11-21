package me.creonc.voxelsmp.features;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import me.creonc.voxelsmp.VoxelSMP;


public class Lifesteal implements Listener {
    private final VoxelSMP plugin;
    private static final double HEART_AMOUNT = 2.0; // 1 heart = 2 HP
    private static final double MIN_HEALTH = 2.0; // Minimum 1 heart
    private boolean enabled = true;  // Toggle for lifesteal behavior

    public Lifesteal(VoxelSMP plugin) {
        this.plugin = plugin;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        plugin.getLogger().info("[Lifesteal] Feature " + (enabled ? "enabled" : "disabled"));
    }

    public boolean isEnabled() {
        return enabled;
    }
    //FIXME: Discord integration
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
        AttributeInstance attribute = player.getAttribute(Attribute.MAX_HEALTH);
        if (attribute != null) {
            double currentMax = attribute.getBaseValue();
            double newMax = Math.max(MIN_HEALTH, currentMax - HEART_AMOUNT);

            plugin.getLogger().info("[Lifesteal] Reducing " + player.getName() + "'s health from " +
                    (currentMax/2) + " hearts to " + (newMax/2) + " hearts");

            // Only eliminate if the new health would be LESS than MIN_HEALTH
            if (currentMax <= MIN_HEALTH) {
                plugin.getLogger().info("[Lifesteal] Player " + player.getName() + " has been eliminated!");
                Bukkit.broadcastMessage("§c" + player.getName() + " has been eliminated!");
                player.kick(Component.text("You have been eliminated!"));
                //TODO: Store ban data
            } else {
                attribute.setBaseValue(newMax);
            }
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
            player.setHealth(newMax);

            player.sendMessage("§a+1 Heart! New max health: " + (newMax/2) + " hearts");
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