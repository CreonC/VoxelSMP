package me.creonc.voxelsmp.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class Settings implements CommandExecutor {
    //register /settings
    @Nonnull
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!command.getName().equals("settings")) {
            return false;
        }

        if (!(sender instanceof Player player)) {
            Bukkit.getLogger().warning("Only players can use this command.");
            return true;
        }

        return true;
    }
}
