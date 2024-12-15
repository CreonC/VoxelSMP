package me.creonc.voxelsmp.commands;

import me.creonc.voxelsmp.config.RolesManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;

public class Settings implements CommandExecutor, Listener {
    private final RolesManager rolesManager;
    private final int invSize = 27;
    private final Component invName = Component.text("Settings");  // Changed to Component

    public Settings(RolesManager rolesManager) {
        this.rolesManager = rolesManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {
        if (!command.getName().equals("settings")) {
            return false;
        }

        if (!(sender instanceof Player player)) {
            Bukkit.getLogger().warning("Only players can use this command.");
            return true;
        }

        openSettingsMenu(player);
        return true;
    }

    private void openSettingsMenu(Player player) {
        Inventory inv = Bukkit.createInventory(player, invSize, invName);

        // Fill with glass panes
        for (int i = 0; i < invSize; i++) {
            inv.setItem(i, createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " "));
        }

        // Add notification toggle
        boolean hasNotifications = rolesManager.hasRole(player.getUniqueId(), "notifications");
        Material toggleMaterial = hasNotifications ? Material.LIME_DYE : Material.GRAY_DYE;
        String toggleStatus = hasNotifications ? "§aEnabled" : "§cDisabled";
        
        ItemStack notificationToggle = createGuiItem(
            toggleMaterial,
            "§6Join Notifications",
            "§7Current status: " + toggleStatus,
            "§7Click to toggle!",
            "§7You'll need your account linked to a Discord account"
        );

        // Place toggle in center of inventory
        inv.setItem(13, notificationToggle);

        player.openInventory(inv);
    }

    private ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            
            List<String> loreList = new ArrayList<>();
            for (String line : lore) {
                loreList.add(line);
            }
            meta.setLore(loreList);
            
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().title().equals(invName)) {
            return;
        }

        event.setCancelled(true); // Prevent any item movement

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (event.getCurrentItem() == null) {
            return;
        }

        // Check if clicked item is the notification toggle
        if (event.getSlot() == 13) {
            boolean currentStatus = rolesManager.hasRole(player.getUniqueId(), "notifications");
            if (currentStatus) {
                rolesManager.removePlayerRole(player.getUniqueId(), "notifications");
                player.sendMessage("§cJoin notifications disabled!");
            } else {
                rolesManager.setPlayerRole(player.getUniqueId(), "notifications");
                player.sendMessage("§aJoin notifications enabled!");
            }
            
            // Refresh the menu
            openSettingsMenu(player);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        // Get the top inventory (our settings menu)
        Inventory topInventory = event.getView().getTopInventory();
        
        // Check if any of the dragged slots are in our settings menu
        for (int slot : event.getRawSlots()) {
            if (slot < topInventory.getSize()) {
                // Cancel if trying to drag into the settings menu
                event.setCancelled(true);
                return;
            }
        }
    }
}