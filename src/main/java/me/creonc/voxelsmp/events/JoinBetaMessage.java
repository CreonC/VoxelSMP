package me.creonc.voxelsmp.events;

import me.creonc.voxelsmp.VoxelSMP;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.Listener;

public class JoinBetaMessage implements Listener {
    private final VoxelSMP plugin;

    public JoinBetaMessage(VoxelSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.sendMessage("Welcome to VoxelSMP Season 4 Beta 2!");
        player.sendMessage("Please report any bugs or issues to the staff team.");
        player.sendMessage("As usual, inventory and world data will be reset after the beta.");
        player.sendMessage("Enjoy your time!");
    }
}
