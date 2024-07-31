package me.creonc.voxelsmp.events;

import me.creonc.voxelsmp.VoxelSMP;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.entity.Player;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class NoGriefDuringGP implements Listener {

    private final VoxelSMP plugin;

    public NoGriefDuringGP(VoxelSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (plugin.gracePeriodActive) {
            Block block = event.getBlock();
            Player breaker = event.getPlayer();

            // Check if the block has metadata about who placed it
            if (block.hasMetadata("placedBy")) {
                String placerName = block.getMetadata("placedBy").get(0).asString();

                // Check if the breaker is not the same as the placer
                if (!breaker.getName().equals(placerName)) {
                    event.setCancelled(true);
                    breaker.sendMessage(ChatColor.RED + "You cannot break blocks placed by " + placerName + " during the grace period!");
                }
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (plugin.gracePeriodActive) {
            Block block = event.getBlock();
            Player placer = event.getPlayer();
            block.setMetadata("placedBy", new FixedMetadataValue(plugin, placer.getName()));
        }
    }
}
