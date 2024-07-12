package me.creonc.voxelsmp.commands;

import me.creonc.voxelsmp.VoxelSMP;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class AutoRestart implements CommandExecutor {
    private final VoxelSMP plugin;

    public AutoRestart(VoxelSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length > 0) {
            String timeArg = strings[0];
            try {
                // Assuming the format is always like "60s"
                int seconds = Integer.parseInt(timeArg.substring(0, timeArg.length() - 1));
                int ticks = seconds * 20; // Convert seconds to ticks

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    // Simulate server restart by broadcasting a message
                    // Actual restart logic depends on your server setup
                    Bukkit.broadcastMessage("Server is restarting...");
                    // Insert actual restart logic here
                }, ticks);

                commandSender.sendMessage("broadcasting message Server will restart in " + seconds + " seconds.");
                Bukkit.broadcastMessage("§l§cServer will restart in " + seconds + " seconds.");

                plugin.AutoRestartActive = true;
                plugin.initAutoRestartBossBar();
                plugin.showAutoRestartBossBar();
            } catch (NumberFormatException e) {
                commandSender.sendMessage("Invalid time format. Please use the format like '60s'.");
            }
            return true;
        } else {
            commandSender.sendMessage("Please specify the restart time. Example: /autorestart 60s");
            return false;
        }
    }
}
