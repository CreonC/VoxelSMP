package me.creonc.voxelsmp.srvintegration;

import java.time.Instant;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.PrivateChannel;
import github.scarsz.discordsrv.dependencies.jda.api.entities.User;
import github.scarsz.discordsrv.util.DiscordUtil;
import me.creonc.voxelsmp.config.RolesManager;
import java.awt.Color;

public class SrvOnPlayerJoined implements Listener {
    private final RolesManager rolesManager;

    public SrvOnPlayerJoined(RolesManager rolesManager) {
        this.rolesManager = rolesManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Fetch the Discord ID linked to the Minecraft player
        String discordID = DiscordSRV.getPlugin() == null || DiscordSRV.getPlugin().getAccountLinkManager() == null
                ? null
                : DiscordSRV.getPlugin().getAccountLinkManager().getDiscordId(event.getPlayer().getUniqueId());

        // If the player doesn't have a linked Discord account
        if (discordID == null) {
            event.getPlayer().sendMessage(
                    "Your account is not linked to a Discord account. It's recommended to link your account to a Discord account");
            event.getPlayer().sendMessage("to receive on-join notifications to protect your account.");
            event.getPlayer().sendMessage("You can link your account to a Discord account by typing /link in the chat");
            return;
        }

        // Fetch the Discord user object
        User user = DiscordUtil.getJda().getUserById(discordID);
        if (user != null) {
            // Check if the player has notifications enabled
            if (rolesManager.hasRole(event.getPlayer().getUniqueId(), "notifications")) {
                // Open a private channel with the user and send an embedded message
                user.openPrivateChannel().queue((PrivateChannel privateChannel) -> {
                    // Create the embed
                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    embedBuilder.setTitle("You joined the server!")
                            .setColor(Color.GREEN) // Set the embed color
                            .setDescription("You have successfully joined the server.")
                            .addField("Name", event.getPlayer().getName(), false) // Player's name
                            .addField("IP Address", event.getPlayer().getAddress().getAddress().getHostAddress(), false) // Player's IP
                            .setTimestamp(Instant.now()) // Current timestamp
                            .setFooter("VoxelSMP Login System", null); // Footer text (optional)

                    // Send the embed message to the private channel
                    privateChannel.sendMessageEmbeds(embedBuilder.build()).queue(
                            success -> Bukkit.getLogger().info("Notification sent to " + user.getName()),
                            failure -> Bukkit.getLogger().info("Failed to send DM to " + user.getName() + ": " + failure.getMessage()));
                });
            }
        } else {
            // If the Discord user is not found
            Bukkit.getLogger().info("Discord user not found for ID: " + discordID);
        }
    }
}
