package me.creonc.voxelsmp.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Arrays;




public class Settings implements CommandExecutor {
    int invSize = 27;
    //register /settings
    @Nonnull
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {

        if (!command.getName().equals("settings")) {
            return false;
        }

        if (!(sender instanceof Player player)) {
            Bukkit.getLogger().warning("Only players can use this command.");
            return true;
        }

        String InvName = "Settings";
        Inventory inv = Bukkit.createInventory(player, invSize, InvName);

        for (int i = 0; i < invSize; i++) {
            inv.setItem(i, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
        }
        
        player.openInventory(inv);

        return true;
    }
}
