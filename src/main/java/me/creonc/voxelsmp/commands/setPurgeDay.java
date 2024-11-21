package me.creonc.voxelsmp.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import me.creonc.voxelsmp.features.PurgeDay;

import java.time.DayOfWeek;

public class setPurgeDay implements CommandExecutor {
    private final PurgeDay purgeDay;

    public setPurgeDay(PurgeDay purgeDay) {
        this.purgeDay = purgeDay;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("voxelsmp.setpurgeday")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage("§cUsage: /setpurgeday <day>");
            return true;
        }

        try {
            DayOfWeek newPurgeDay = DayOfWeek.valueOf(args[0].toUpperCase());
            purgeDay.setPurgeDay(newPurgeDay);
            sender.sendMessage("§aPurge day has been set to: " + newPurgeDay);
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§cInvalid day! Use: MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, or SUNDAY");
        }

        return true;
    }
}