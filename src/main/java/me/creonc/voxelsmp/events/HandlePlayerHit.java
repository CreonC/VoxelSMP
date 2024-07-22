package me.creonc.voxelsmp.events;

import me.creonc.voxelsmp.VoxelSMP;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class HandlePlayerHit implements Listener {
    private final VoxelSMP plugin;

    public HandlePlayerHit(VoxelSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void EntityDamageByEntityEvent(EntityDamageByEntityEvent event){
        if (plugin.gracePeriodActive) {
            if (event.getEntity() instanceof Player) {
                Entity attacker = event.getDamager();
                if (attacker instanceof Player) {
                    attacker.sendMessage(ChatColor.RED + "You cannot attack players during grace period.");
                    event.setCancelled(true);
                }
            }
        }


    }
}
