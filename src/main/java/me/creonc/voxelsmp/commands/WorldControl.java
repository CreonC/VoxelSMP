package me.creonc.voxelsmp.commands;

import me.creonc.voxelsmp.VoxelSMP;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

public class WorldControl implements CommandExecutor, Listener {
    private final VoxelSMP plugin;
    private boolean allowNether = true;  // Global setting for Nether access
    private boolean allowEnd = true;      // Global setting for End access

    public WorldControl(VoxelSMP plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("Only players can use this command.");
            return true;
        }
        Bukkit.getLogger().info("Command executed by " + commandSender.getName() + " with args: " + String.join(" ", args));

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /worldcontrol <enablenether | disablenether | enableend | disableend>");
            return false; // Not enough arguments
        }


        switch (args[0].toLowerCase()) {
            case "enablenether":
                Bukkit.getLogger().info("Debug: Enabling Nether access.");
                allowNether = true;
                Bukkit.broadcastMessage("Nether access has been enabled.");
                return true;
            case "disablenether":
                Bukkit.getLogger().info("Debug: Disabling Nether access.");
                allowNether = false;
                Bukkit.broadcastMessage("Nether access has been disabled.");
                return true;
            case "enableend":
                Bukkit.getLogger().info("Debug: Enabling End access.");
                allowEnd = true;
                Bukkit.broadcastMessage("End access has been enabled.");
                return true;
            case "disableend":
                Bukkit.getLogger().info("Debug: Disabling End access.");
                allowEnd = false;
                Bukkit.broadcastMessage("End access has been disabled.");
                return true;
            default:
                Bukkit.getLogger().info("Debug: Command not recognized.");
                player.sendMessage(ChatColor.RED + "Usage: /worldcontrol <enablenether | disablenether | enableend | disableend>");
                return false; // Command not recognized
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Location toLocation = event.getTo();

        // Log teleport cause for debugging
        PlayerTeleportEvent.TeleportCause cause = event.getCause();

        // Check for Nether access
        if (cause == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            if (!allowNether) {
                event.setCancelled(true);
                player.sendMessage("Nether access is currently disabled.");
            }
        }

        // Check for End access
        if ((cause == PlayerTeleportEvent.TeleportCause.END_PORTAL || cause == PlayerTeleportEvent.TeleportCause.END_GATEWAY)) {
            if (!allowEnd) {
                event.setCancelled(true);
                player.sendMessage("End access is currently disabled.");
            }
        }
    }
}
