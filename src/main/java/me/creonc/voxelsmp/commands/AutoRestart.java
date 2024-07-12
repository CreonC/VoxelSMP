package me.creonc.voxelsmp.commands;

import me.creonc.voxelsmp.VoxelSMP;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

public class AutoRestart implements CommandExecutor {
    private final VoxelSMP plugin;
    private BukkitTask restartTask;

    public AutoRestart(VoxelSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length > 0) {
            String timeArg = strings[0];
            try {
                int seconds = Integer.parseInt(timeArg.substring(0, timeArg.length() - 1));
                commandSender.sendMessage("Server will restart in " + seconds + " seconds.");
                scheduleRestart(seconds);
                return true;
            } catch (NumberFormatException e) {
                commandSender.sendMessage("Invalid time format. Please use the format like '60s'.");
                return false;
            }
        } else {
            commandSender.sendMessage("Please specify the restart time. Example: /autorestart 60s");
            return false;
        }
    }

    private void scheduleRestart(int seconds) {
        final int[] remainingSeconds = {seconds};
        restartTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (remainingSeconds[0] > 0) {
                if (remainingSeconds[0] <= 10 || remainingSeconds[0] % 10 == 0) {
                    Bukkit.broadcastMessage("§l§cServer will restart in " + remainingSeconds[0] + " seconds.");
                }
                remainingSeconds[0]--;
            } else {
                Bukkit.broadcastMessage("§l§cServer is restarting...");
                Bukkit.getServer().shutdown();
                restartTask.cancel();
            }
        }, 0L, 20L); // Schedule to run every second (20 ticks)
    }
}
