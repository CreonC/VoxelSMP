package me.creonc.voxelsmp;

import me.creonc.voxelsmp.commands.AutoRestart;
import me.creonc.voxelsmp.commands.GracePeriodCommand;
import me.creonc.voxelsmp.tabcomplete.AutoComplete;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Boss;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import me.creonc.voxelsmp.commands.Settings;
import me.creonc.voxelsmp.events.HandlePlayerHit;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public final class VoxelSMP extends JavaPlugin {
    long gracePeriodExpiration = 0;
    private BossBar gracePeriodBossBar;
    public BukkitTask gracePeriodUpdateTask;
    long gracePeriodDuration = 0;
    public boolean gracePeriodActive = false;

    private BossBar AutoRestartBossBar;
    public BukkitTask AutoRestartUpdateTask;
    long AutoRestartDuration = 0;
    public boolean AutoRestartActive = false;

    @Override
    public void onEnable() {
        long StartupTime = System.currentTimeMillis();
        // Plugin startup logic
        Bukkit.getLogger().info("Starting VoxelSMP core");
        getConfig().options().copyDefaults(true);
        this.saveConfig();
        Settings settings = new Settings();
        Logger pluginLogger = getLogger();

        try {
            pluginLogger.info("Loading VoxelSMP commands");
            // Settings
            this.getCommand("settings").setExecutor(settings);

            // Grace Period
            GracePeriodCommand command = new GracePeriodCommand(this);
            PluginCommand gpCommand = getCommand("graceperiod");
            gpCommand.setExecutor(command);
            // AutoRestart

            GracePeriodCommand ar = new GracePeriodCommand(this);
            PluginCommand autoRestart = getCommand("autorestart");
            autoRestart.setExecutor(ar);

            pluginLogger.info("VoxelSMP commands loaded successfully");
            pluginLogger.info("Loading VoxelSMP tab completions");
            AutoComplete tabCompleter = new AutoComplete();
            gpCommand.setTabCompleter(tabCompleter);
            pluginLogger.info("VoxelSMP tab completions loaded successfully");
            pluginLogger.info("Loading VoxelSMP events");
            getServer().getPluginManager().registerEvents(new HandlePlayerHit(this), this);
            pluginLogger.info("VoxelSMP events loaded successfully");
            pluginLogger.info("VoxelSMP core started successfully in " + (System.currentTimeMillis() - StartupTime) + "ms");
        }
        catch (NullPointerException e) {
            pluginLogger.severe("Failed to initialize VoxelSMP Core. Reason: " + e.getCause());
        }


    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
        gracePeriodBossBar.setVisible(false);
        if (gracePeriodUpdateTask != null) {
            gracePeriodUpdateTask.cancel();
        }
        Bukkit.getLogger().info("Stopping VoxelSMP core");
        saveConfig();
    }

    public void initGracePeriodBossBar() {
        Bukkit.getLogger().info("[VOXELSMP-Debug] Initializing Grace Period Boss Bar (CreatingBossBar)");
        gracePeriodBossBar = Bukkit.createBossBar("Grace Period: 0s", BarColor.GREEN, BarStyle.SOLID);
        gracePeriodBossBar.setVisible(true);

        gracePeriodUpdateTask = Bukkit.getScheduler().runTaskTimer(this, this::updateGracePeriodBossBar, 0L, 10L); // Update every 0.5 seconds
    }

    public void initAutoRestartBossBar() {
        Bukkit.getLogger().info("[VOXELSMP-Debug] Initializing AutoRestart Boss Bar (CreatingBossBar)");
        AutoRestartBossBar = Bukkit.createBossBar("AutoRestart: 0s", BarColor.RED, BarStyle.SOLID);
        AutoRestartBossBar.setVisible(true);

        AutoRestartUpdateTask = Bukkit.getScheduler().runTaskTimer(this, this::updateAutoRestartBossBar, 0L, 10L); // Update every 0.5 seconds
    }

    private void updateAutoRestartBossBar() {
        long remaining = getAutoRestartDurationRemaining();
        if (remaining > 0) {
            double progress = (double) remaining / AutoRestartDuration;
            AutoRestartBossBar.setProgress(progress);
            AutoRestartBossBar.setTitle("Restart: " + formatTime(remaining));
            AutoRestartBossBar.setVisible(true);
            AutoRestartActive = true;
            if (progress < 0.15) {
                AutoRestartBossBar.setColor(BarColor.RED);
            } else if (progress < 0.5) {
                AutoRestartBossBar.setColor(BarColor.YELLOW);
            } else {
                AutoRestartBossBar.setColor(BarColor.GREEN);
            }
        }
    }


    private void updateGracePeriodBossBar() {
        long remaining = getGracePeriodRemaining();
        if (remaining > 0) {
            double progress = (double) remaining / gracePeriodDuration;
            gracePeriodBossBar.setProgress(progress);
            gracePeriodBossBar.setTitle("Grace Period: " + formatTime(remaining));
            gracePeriodBossBar.setVisible(true);
            gracePeriodActive = true;
            if (progress < 0.15) {
                gracePeriodBossBar.setColor(BarColor.RED);
            } else if (progress < 0.5) {
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
            String message = "§l§cGrace period has expired.";
            Bukkit.broadcastMessage(message);
        }
    }

    public void showGracePeriodBossBar() {
        gracePeriodBossBar.setVisible(true);
    }

    public void hideGracePeriodBossBar() {
        gracePeriodBossBar.setVisible(false);
    }

    public void showAutoRestartBossBar() {
        AutoRestartBossBar.setVisible(true);
    }



    public void setGracePeriod(long duration) {
        gracePeriodExpiration = System.currentTimeMillis() + duration;
        gracePeriodDuration = duration;
    }

    public long getGracePeriodRemaining() {
        return gracePeriodExpiration - System.currentTimeMillis();
    }

    public long getAutoRestartDurationRemaining() {
        return AutoRestartDuration - System.currentTimeMillis();
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
