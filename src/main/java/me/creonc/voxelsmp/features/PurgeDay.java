package me.creonc.voxelsmp.features;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import me.creonc.voxelsmp.VoxelSMP;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PurgeDay implements Listener {
    private final VoxelSMP plugin;
    private final Lifesteal lifesteal;
    private final Logger logger;
    private final Configuration config;
    private final AtomicBoolean isCheckerRunning = new AtomicBoolean(false);

    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_SECONDS = 5;
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private String apiBaseUrl;
    private String apiStatusEndpoint;

    private static final Gson gson = new Gson();
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .build();

    private volatile boolean isPurgeActive = false;
    private volatile String nextPurgeStart = "Not scheduled";
    private volatile String nextPurgeEnd = "Not scheduled";
    private volatile BukkitRunnable purgeChecker;
    private boolean isInitialized = false;

    private static class PurgeStatus {
        @SerializedName("is_active")
        boolean isActive;

        @SerializedName("next_purge_start")
        String nextPurgeStart;

        @SerializedName("next_purge_end")
        String nextPurgeEnd;

        boolean isValid() {
            return nextPurgeStart != null && nextPurgeEnd != null;
        }
    }

    public PurgeDay(VoxelSMP plugin, Lifesteal lifesteal) {
        this.plugin = plugin;
        this.lifesteal = lifesteal;
        this.logger = plugin.getLogger();
        this.config = plugin.getConfig();

        loadConfiguration();
        startPurgeChecker();
    }

    private void loadConfiguration() {
        apiBaseUrl = config.getString("purge.api.baseUrl", "http://192.168.1.225:10198"); //Yes I love leaking my local IP
        apiStatusEndpoint = config.getString("purge.api.statusEndpoint", "/api/purge/status");

        if (!isValidUrl(apiBaseUrl)) {
            logger.severe("[PurgeDay] Invalid API base URL in configuration!");
            return;
        }
    }

    private boolean isValidUrl(String url) {
        try {
            new URI(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void startPurgeChecker() {
        if (!isCheckerRunning.compareAndSet(false, true)) {
            logger.warning("[PurgeDay] Attempted to start another purge checker while one is already running");
            return;
        }

        purgeChecker = new BukkitRunnable() {
            @Override
            public void run() {
                checkPurgeStatus();
            }
        };

        purgeChecker.runTaskTimerAsynchronously(plugin, 0L, 20L * 30);
    }

    private void checkPurgeStatus() {
        try {
            String url = apiBaseUrl + apiStatusEndpoint;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(TIMEOUT)
                    .GET()
                    .build();

            HttpResponse<String> response = sendRequestWithRetry(request);

            if (response.statusCode() == 200) {
                PurgeStatus status = gson.fromJson(response.body(), PurgeStatus.class);

                if (status != null && status.isValid()) {
                    executeSync(() -> updatePurgeStatus(status));
                } else {
                    logger.warning("[PurgeDay] Received invalid status from API");
                }
            } else {
                throw new RuntimeException("API returned status code: " + response.statusCode());
            }

        } catch (Exception e) {
            handleError("Failed to check purge status", e);
        }
    }

    private HttpResponse<String> sendRequestWithRetry(HttpRequest request) throws Exception {
        Exception lastException = null;

        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            try {
                return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (Exception e) {
                lastException = e;
                if (attempt < MAX_RETRIES - 1) {
                    Thread.sleep(RETRY_DELAY_SECONDS * 1000L);
                }
            }
        }

        throw lastException;
    }

    private String formatDateTime(String isoDateTime) {
        try {
            OffsetDateTime dateTime = OffsetDateTime.parse(isoDateTime);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d 'at' h:mm a");
            return dateTime.format(formatter) + " (UTC+8)";
        } catch (Exception e) {
            logger.warning("[PurgeDay] Failed to parse datetime: " + isoDateTime);
            return isoDateTime;
        }
    }

    private void updatePurgeStatus(PurgeStatus status) {
        if (!isInitialized || status.isActive != isPurgeActive ||
                !status.nextPurgeStart.equals(nextPurgeStart) ||
                !status.nextPurgeEnd.equals(nextPurgeEnd)) {

            logger.info("[PurgeDay] Purge status changed:");
            logger.info("[PurgeDay] Active: " + status.isActive);
            logger.info("[PurgeDay] Next purge starts: " + formatDateTime(status.nextPurgeStart));
            logger.info("[PurgeDay] Next purge ends: " + formatDateTime(status.nextPurgeEnd));

            isPurgeActive = status.isActive;
            nextPurgeStart = status.nextPurgeStart;
            nextPurgeEnd = status.nextPurgeEnd;

            lifesteal.setEnabled(isPurgeActive);

            if (isInitialized) {
                if (isPurgeActive) {
                    announcePurgeStart();
                } else {
                    announcePurgeEnd();
                }
            }

            isInitialized = true;
        }
    }

    private void executeAsync(Runnable task) {
        new BukkitRunnable() {
            @Override
            public void run() {
                task.run();
            }
        }.runTaskAsynchronously(plugin);
    }

    private void executeSync(Runnable task) {
        new BukkitRunnable() {
            @Override
            public void run() {
                task.run();
            }
        }.runTask(plugin);
    }

    private void handleError(String message, Exception e) {
        executeSync(() -> {
            logger.log(Level.SEVERE, "[PurgeDay] " + message, e);
        });
    }

    public void shutdown() {
        if (purgeChecker != null) {
            purgeChecker.cancel();
        }
        isCheckerRunning.set(false);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (isPurgeActive) {
            event.getPlayer().sendMessage("§c§lWARNING: The Purge is currently active!");
            event.getPlayer().sendMessage("§cLifesteal is enabled - Be careful!");
            event.getPlayer().sendMessage("§cPurge ends at " + formatDateTime(nextPurgeEnd));
        } else {
            event.getPlayer().sendMessage("§aNext purge starts on " + formatDateTime(nextPurgeStart));
            event.getPlayer().sendMessage("§aand ends on " + formatDateTime(nextPurgeEnd));
        }
    }

    private void announcePurgeStart() {
        Bukkit.broadcastMessage("§c=========================");
        Bukkit.broadcastMessage("§4§lTHE PURGE HAS BEGUN!");
        Bukkit.broadcastMessage("§cLifesteal is now active!");
        Bukkit.broadcastMessage("§cKill other players to steal their hearts!");
        Bukkit.broadcastMessage("§cEnds at " + formatDateTime(nextPurgeEnd));
        Bukkit.broadcastMessage("§c=========================");

        Bukkit.getOnlinePlayers().forEach(player ->
                player.playSound(player.getLocation(), "entity.ender_dragon.growl", 1.0f, 1.0f)
        );
    }

    private void announcePurgeEnd() {
        Bukkit.broadcastMessage("§a=========================");
        Bukkit.broadcastMessage("§2§lTHE PURGE HAS ENDED!");
        Bukkit.broadcastMessage("§aLifesteal is now disabled.");
        Bukkit.broadcastMessage("§aYou get to live another day!");
        Bukkit.broadcastMessage("§aNext purge starts " + formatDateTime(nextPurgeStart));
        Bukkit.broadcastMessage("§a=========================");

        Bukkit.getOnlinePlayers().forEach(player ->
                player.playSound(player.getLocation(), "entity.player.levelup", 1.0f, 1.0f)
        );
    }
}