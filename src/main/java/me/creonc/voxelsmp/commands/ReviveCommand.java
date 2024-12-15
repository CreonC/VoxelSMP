package me.creonc.voxelsmp.commands;

import me.creonc.voxelsmp.VoxelSMP;
import me.creonc.voxelsmp.features.Lifesteal;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;

public class ReviveCommand implements CommandExecutor {
    private final VoxelSMP plugin;
    private final Lifesteal lifesteal;

    public ReviveCommand(VoxelSMP plugin, Lifesteal lifesteal) {
        this.plugin = plugin;
        this.lifesteal = lifesteal;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("voxelsmp.revive")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage("§cUsage: /revive <player>");
            return true;
        }

        String targetName = args[0];
        lifesteal.revivePlayer(targetName);
        
        // Broadcast the revival
        Bukkit.broadcast(
            Component.text("§a" + targetName + " has been revived!"),
            "voxelsmp.revive.notify"
        );
        
        plugin.getLogger().info("[Lifesteal] " + sender.getName() + " revived player: " + targetName);
        return true;
    }
}
