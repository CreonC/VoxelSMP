package me.creonc.voxelsmp.srvintegration;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.dependencies.jda.api.entities.User;
import github.scarsz.discordsrv.util.DiscordUtil;
import me.creonc.voxelsmp.config.RolesManager;

public class SrvOnPlayerJoined implements Listener  {
    private final RolesManager rolesManager;

    public SrvOnPlayerJoined(RolesManager rolesManager) {
        this.rolesManager = rolesManager;
    }

    @EventHandler
    public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
        String discordID = DiscordSRV.getPlugin() == null || DiscordSRV.getPlugin().getAccountLinkManager() == null ? null : DiscordSRV.getPlugin().getAccountLinkManager().getDiscordId(event.getPlayer().getUniqueId());
        if (discordID == null) {
            event.getPlayer().sendMessage("You account is not linked to a Discord account. It's recommended to link your account to a Discord account");
            event.getPlayer().sendMessage("to receive on join notifications to protect your account.");
            event.getPlayer().sendMessage("You can link your account to a Discord account by typing /link in the chat");
            return;
        }

        User user = DiscordUtil.getJda().getUserById(discordID);
        if (user != null) {
            // Check if player has notifications enabled
            if (rolesManager.hasRole(event.getPlayer().getUniqueId(), "notifications")) {
                Bukkit.getServer().getLogger().info("TODO: Send notification to " + user.getName() + "With message " + event.getPlayer().getName() + " joined the server with ip " + event.getPlayer().getAddress().toString());
            }
        }
    }
}
