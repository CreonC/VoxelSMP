package me.creonc.voxelsmp.commands;

import me.creonc.voxelsmp.VoxelSMP;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;


public class DeferRestart implements CommandExecutor {
    private final VoxelSMP plugin;
    private BukkitTask restartTask;

    public DeferRestart(VoxelSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        commandSender.sendMessage("Server will restart once everyone has left the server.");

        // Cancel any existing restart task
        if (restartTask != null && !restartTask.isCancelled()) {
            commandSender.sendMessage("Restart task already scheduled. Cancelling the existing task.");
            restartTask.cancel();
        }

        restartTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (Bukkit.getOnlinePlayers().isEmpty()) {
                    // No players online, restart the server
                    Bukkit.getLogger().info("No players online. Restarting server...");
                    Bukkit.getServer().shutdown();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L * 5); // Check every 5 secs (20 ticks * 5 seconds)

        return true;
    }
}
