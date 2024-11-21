package me.creonc.voxelsmp.features;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import me.creonc.voxelsmp.VoxelSMP;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.DayOfWeek;
import java.time.Duration;
import java.util.logging.Logger;

public class PurgeDay implements Listener {
    private final VoxelSMP plugin;
    private final Lifesteal lifesteal;
    private final Logger logger;
    private static final String API_URL = "http://localhost:8080/api/purge/status";
    private static final String API_UPDATE_URL = "http://localhost:8080/api/purge/day/";
    private static final Gson gson = new Gson();
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private boolean isPurgeActive = false;
    private String nextPurge = "";
    private String purgeDay = "";

    private static class PurgeStatus {
        @SerializedName("is_active")
        boolean isActive;

        @SerializedName("next_purge")
        String nextPurge;

        @SerializedName("purge_day")
        String purgeDay;
    }

    public PurgeDay(VoxelSMP plugin, Lifesteal lifesteal) {
        this.plugin = plugin;
        this.lifesteal = lifesteal;
        this.logger = plugin.getLogger();
        startPurgeChecker();
    }

    public void setPurgeDay(DayOfWeek newDay) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(API_UPDATE_URL + newDay.toString()))
                            .timeout(Duration.ofSeconds(5))
                            .PUT(HttpRequest.BodyPublishers.noBody())
                            .build();

                    HttpResponse<String> response = httpClient.send(request,
                            HttpResponse.BodyHandlers.ofString());

                    // Run the response handling on the main thread
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (response.statusCode() == 200) {
                                logger.info("[PurgeDay] Successfully updated purge day to " + newDay);
                                // Force an immediate status check
                                checkPurgeStatus();
                            } else {
                                logger.severe("[PurgeDay] Failed to update purge day. Response code: "
                                        + response.statusCode());
                            }
                        }
                    }.runTask(plugin);

                } catch (Exception e) {
                    // Run error handling on the main thread
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            logger.severe("[PurgeDay] Failed to update purge day: " + e.getMessage());
                        }
                    }.runTask(plugin);
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    private void startPurgeChecker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                checkPurgeStatus();
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 20L * 30); // Check every 30 seconds
    }

    private void checkPurgeStatus() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                PurgeStatus status = gson.fromJson(response.body(), PurgeStatus.class);

                // Run the status update on the main thread
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        updatePurgeStatus(status);
                    }
                }.runTask(plugin);
            } else {
                throw new RuntimeException("API returned status code: " + response.statusCode());
            }

        } catch (Exception e) {
            // Run error handling on the main thread
            new BukkitRunnable() {
                @Override
                public void run() {
                    logger.severe("[PurgeDay] Failed to check purge status: " + e.getMessage());
                }
            }.runTask(plugin);
        }
    }

    private void updatePurgeStatus(PurgeStatus status) {
        if (status.isActive != isPurgeActive) {
            isPurgeActive = status.isActive;
            nextPurge = status.nextPurge;
            purgeDay = status.purgeDay;
            lifesteal.setEnabled(isPurgeActive);

            if (isPurgeActive) {
                announcePurgeStart();
            } else {
                announcePurgeEnd();
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (isPurgeActive) {
            event.getPlayer().sendMessage("§c§lWARNING: The Purge is currently active!");
            event.getPlayer().sendMessage("§cLifesteal is enabled - Be careful!");
        } else {
            event.getPlayer().sendMessage("§aThe server is peaceful. Next purge starts on " + purgeDay);
        }
    }

    private void announcePurgeStart() {
        Bukkit.broadcastMessage("§c=========================");
        Bukkit.broadcastMessage("§4§lTHE PURGE HAS BEGUN!");
        Bukkit.broadcastMessage("§cLifesteal is now active!");
        Bukkit.broadcastMessage("§cKill other players to steal their hearts!");
        Bukkit.broadcastMessage("§c=========================");

        Bukkit.getOnlinePlayers().forEach(player ->
                player.playSound(player.getLocation(), "entity.ender_dragon.growl", 1.0f, 1.0f)
        );
    }

    private void announcePurgeEnd() {
        Bukkit.broadcastMessage("§a=========================");
        Bukkit.broadcastMessage("§2§lTHE PURGE HAS ENDED!");
        Bukkit.broadcastMessage("§aLifesteal is now disabled.");
        Bukkit.broadcastMessage("§aReturn to your peaceful activities.");
        Bukkit.broadcastMessage("§a=========================");

        Bukkit.getOnlinePlayers().forEach(player ->
                player.playSound(player.getLocation(), "entity.player.levelup", 1.0f, 1.0f)
        );
    }
}