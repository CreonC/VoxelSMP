package me.creonc.voxelsmp.commands;

import me.creonc.voxelsmp.VoxelSMP;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class doubleEXP implements CommandExecutor {

    private final VoxelSMP plugin;

    public doubleEXP(VoxelSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) {
            sender.sendMessage("§cUsage: /doublexp on|off");
            return true;
        }

        String arg = args[0].toLowerCase();

        if (arg.equals("on")) {
            plugin.setDoubleEXPEnabled(true);
            sender.sendMessage("§aDouble experience has been enabled.");
        } else if (arg.equals("off")) {
            plugin.setDoubleEXPEnabled(false);
            sender.sendMessage("§cDouble experience has been disabled.");
        } else {
            sender.sendMessage("§cInvalid argument. Usage: /doublexp on|off");
        }

        return true;
    }
}
