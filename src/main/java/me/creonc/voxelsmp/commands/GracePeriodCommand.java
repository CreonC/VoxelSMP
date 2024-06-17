package me.creonc.voxelsmp.commands;

import me.creonc.voxelsmp.VoxelSMP;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class GracePeriodCommand implements CommandExecutor {
    private final VoxelSMP plugin;

    public GracePeriodCommand(VoxelSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "set":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "[VoxelSMP] Usage: /graceperiod set time(int) unit(S/M/H/D)");
                    return true;
                }

                int time = Integer.parseInt(args[1].replaceAll("[^0-9]", ""));
                String unit = args[1].replaceAll("[0-9]", "");

                long duration = convertToMillis(time, unit);
                plugin.setGracePeriod(duration);
                sender.sendMessage(ChatColor.GREEN + "[VoxelSMP] Global grace period set for " + time + " " + unit + ".");
                sender.sendMessage(ChatColor.GREEN + "[VoxelSMP] Global grace period active.");
                if (plugin.gracePeriodActive) {
                    plugin.gracePeriodUpdateTask.cancel();
                    plugin.hideGracePeriodBossBar();
                }
                plugin.gracePeriodActive = true;
                plugin.initGracePeriodBossBar();
                plugin.showGracePeriodBossBar();
                break;

            case "execute":
                long remaining = plugin.getGracePeriodRemaining();
                if (remaining <= 0) {
                    sender.sendMessage(ChatColor.RED + "[VoxelSMP] There is no active grace period.");
                    return true;
                }

                sender.sendMessage(ChatColor.GREEN + "[VoxelSMP] The grace period will expire in " + plugin.formatTime(remaining) + ".");
                break;

            case "remove":
                plugin.setGracePeriod(0);
                sender.sendMessage(ChatColor.GREEN + "[VoxelSMP] The grace period has been removed.");
                plugin.hideGracePeriodBossBar();
                plugin.gracePeriodUpdateTask.cancel();
                plugin.gracePeriodActive = false;
                break;
            case "query":
                if (plugin.getGracePeriodRemaining() <= 0) {
                    sender.sendMessage(ChatColor.RED + "[VoxelSMP] There is no active grace period.");
                    return true;
                }

                sender.sendMessage(ChatColor.GREEN + "[VoxelSMP] The grace period will expire in " + plugin.formatTime(plugin.getGracePeriodRemaining()) + ".");
                break;

            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "[VoxelSMP] Yo I see you don't have any args, here is some help:");
        sender.sendMessage(ChatColor.GRAY + "/graceperiod set <time><unit(S/M/H/D)> - Set a global grace period");
        sender.sendMessage(ChatColor.GRAY + "/graceperiod execute - Runs the remaining grace period");
        sender.sendMessage(ChatColor.GRAY + "/graceperiod remove - Remove the grace period");
        sender.sendMessage(ChatColor.GRAY + "/graceperiod query - Check the remaining grace period");
    }

    private long convertToMillis(int time, String unit) {
        switch (unit.toUpperCase()) {
            case "S":
                return TimeUnit.SECONDS.toMillis(time);
            case "M":
                return TimeUnit.MINUTES.toMillis(time);
            case "H":
                return TimeUnit.HOURS.toMillis(time);
            case "D":
                return TimeUnit.DAYS.toMillis(time);
            default:
                return 0;
        }
    }
}
