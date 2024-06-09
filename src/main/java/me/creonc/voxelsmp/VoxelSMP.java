package me.creonc.voxelsmp;

import me.creonc.voxelsmp.commands.GracePeriodCommand;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import me.creonc.voxelsmp.commands.Settings;
import me.creonc.voxelsmp.events.HandlePlayerHit;
import org.bukkit.scheduler.BukkitTask;

import java.util.BitSet;
import java.util.concurrent.TimeUnit;

public final class VoxelSMP extends JavaPlugin {
    long gracePeriodExpiration = 0;
    private BossBar gracePeriodBossBar;
    public BukkitTask gracePeriodUpdateTask;
    long gracePeriodDuration = 0;
    public boolean gracePeriodActive = false;

    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getLogger().info("Starting VoxelSMP core");
        getConfig().options().copyDefaults(true);
        this.saveConfig();
        Settings settings = new Settings();

        try {
            Bukkit.getLogger().info("Loading VoxelSMP commands");
            this.getCommand("settings").setExecutor(settings);
            GracePeriodCommand command = new GracePeriodCommand(this);
            getCommand("graceperiod").setExecutor(command);
            Bukkit.getLogger().info("VoxelSMP commands loaded successfully");
            Bukkit.getLogger().info("Loading VoxelSMP events");
            getServer().getPluginManager().registerEvents(new HandlePlayerHit(this), this);
            Bukkit.getLogger().info("VoxelSMP events loaded successfully");
        }
        catch (NullPointerException e) {
            Bukkit.getLogger().severe("Failed to initialize VoxelSMP Core. Reason: " + e.getCause());
        }


    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
        gracePeriodBossBar.setVisible(false);
        gracePeriodUpdateTask.cancel();
        Bukkit.getLogger().info("Stopping VoxelSMP core");
        saveConfig();
    }

    public void initGracePeriodBossBar() {
        Bukkit.getLogger().info("[VOXELSMP-Debug] Initializing Grace Period Boss Bar (CreatingBossBar)");
        gracePeriodBossBar = Bukkit.createBossBar("Grace Period: 0s", BarColor.GREEN, BarStyle.SOLID);
        gracePeriodBossBar.setVisible(true);

        gracePeriodUpdateTask = Bukkit.getScheduler().runTaskTimer(this, this::updateGracePeriodBossBar, 0L, 10L); // Update every 0.5 seconds
    }


    private void updateGracePeriodBossBar() {
        long remaining = getGracePeriodRemaining();
        if (remaining > 0) {
            double progress = (double) remaining / gracePeriodDuration;
            gracePeriodBossBar.setProgress(progress);
            gracePeriodBossBar.setTitle("Grace Period: " + formatTime(remaining));
            gracePeriodBossBar.setVisible(true);
            gracePeriodActive = true;
            if (progress < 0.3) {
                gracePeriodBossBar.setColor(BarColor.RED);
            } else if (progress < 0.6) {
                gracePeriodBossBar.setColor(BarColor.YELLOW);
            } else {
                gracePeriodBossBar.setColor(BarColor.GREEN);
            }


            for (Player player : Bukkit.getOnlinePlayers()) {
                gracePeriodBossBar.addPlayer(player);
            }

        } else {
            gracePeriodBossBar.setTitle("Grace Period: EXPIRED");
            Bukkit.getScheduler().runTaskLater(this, () -> {
                gracePeriodBossBar.setVisible(false);
            }, 100L); // Hide boss bar after 5 seconds (100 ticks
            gracePeriodUpdateTask.cancel();
            gracePeriodActive = false;
        }
    }

    public void showGracePeriodBossBar() {
        gracePeriodBossBar.setVisible(true);
    }

    public void hideGracePeriodBossBar() {
        gracePeriodBossBar.setVisible(false);
    }



    public void setGracePeriod(long duration) {
        gracePeriodExpiration = System.currentTimeMillis() + duration;
        gracePeriodDuration = duration;
    }

    public long getGracePeriodRemaining() {
        return gracePeriodExpiration - System.currentTimeMillis();
    }

    public String formatTime(long millis) {
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        long minutes = TimeUnit.SECONDS.toMinutes(seconds);
        long hours = TimeUnit.MINUTES.toHours(minutes);
        long days = TimeUnit.HOURS.toDays(hours);

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("d ");
        }
        if (hours > 0) {
            sb.append(hours % 24).append("h ");
        }
        if (minutes > 0) {
            sb.append(minutes % 60).append("m ");
        }
        if (seconds > 0) {
            sb.append(seconds % 60).append("s");
        }
        return sb.toString().trim();
    }

}
