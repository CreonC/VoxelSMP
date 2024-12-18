package me.creonc.voxelsmp.events;

import me.creonc.voxelsmp.VoxelSMP;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;

public class EXPListener implements Listener {

    private final VoxelSMP plugin;

    public EXPListener(VoxelSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerExpChange(PlayerExpChangeEvent event) {
        if (plugin.isDoubleEXPEnabled()) {
            int currentAmount = event.getAmount();
            event.setAmount(currentAmount * 2);
        }
    }
}
