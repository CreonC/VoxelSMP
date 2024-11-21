package me.creonc.voxelsmp.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class BanFeather implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.getClientBrandName().contains("Feather Fabric")) {
            player.kickPlayer("Feather client is not allowed on this server.");
        } else {
            // Log to console the client brand name of the player if not using Feather Fabric
            Bukkit.getLogger().info("[Voxel-Debug]: " + player.getName() + " is using " + player.getClientBrandName());

            if (player.getClientBrandName().contains("Fabric")) { // Fabric can sometimes be used with other mods that creates an unfair advantage
                player.sendMessage("Blacklisted modifications that create an unfair advantage are not allowed on this server.");
            }
        }
    }
}