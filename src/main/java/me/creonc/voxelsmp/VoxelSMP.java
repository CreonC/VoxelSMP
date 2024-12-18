package me.creonc.voxelsmp;


import github.scarsz.discordsrv.dependencies.jda.api.managers.Manager;
import me.creonc.voxelsmp.commands.*;
import me.creonc.voxelsmp.config.ConfigManager;
import me.creonc.voxelsmp.config.RolesManager;
import me.creonc.voxelsmp.events.BanFeather;
import me.creonc.voxelsmp.events.EXPListener;
import me.creonc.voxelsmp.events.NoGriefDuringGP;
import me.creonc.voxelsmp.features.Lifesteal;
import me.creonc.voxelsmp.features.PurgeDay;
import me.creonc.voxelsmp.tabcomplete.AutoComplete;
import me.creonc.voxelsmp.tabcomplete.AutoCompleteNether;
import me.creonc.voxelsmp.srvintegration.SrvOnPlayerJoined;

import github.scarsz.discordsrv.DiscordSRV;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
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
    public Lifesteal lifesteal;
    private boolean isDoubleEXPEnabled = false;


    @Override
    public void onEnable() {
        long StartupTime = System.currentTimeMillis();
        Logger pluginLogger = getLogger();
        // Plugin startup logic
        pluginLogger.info("Starting VoxelSMP core");
        try {
            pluginLogger.info("Initializing VoxelSMP config manager");
            ConfigManager configManager = new ConfigManager(this);
            pluginLogger.info("Initialized VoxelSMP config manager");
            RolesManager rolesManager = new RolesManager(this);

            // DiscordSRV Integration
            if (getServer().getPluginManager().getPlugin("DiscordSRV") != null) {
                pluginLogger.info("DiscordSRV found. Enabling DiscordSRV integration");
                getServer().getPluginManager().registerEvents(new SrvOnPlayerJoined(rolesManager), this);
            } else {
                pluginLogger.warning("DiscordSRV not found. DiscordSRV integration disabled");
            }

            pluginLogger.info("Loading VoxelSMP commands");
            // Settings
            Settings settings = new Settings(rolesManager);
            this.getCommand("settings").setExecutor(settings);
            getServer().getPluginManager().registerEvents(settings, this); // Register the listener
            // Grace Period
            GracePeriodCommand command = new GracePeriodCommand(this);
            PluginCommand gpCommand = getCommand("graceperiod");
            gpCommand.setExecutor(command);
            // AutoRestart
            AutoRestart ar = new AutoRestart(this);
            PluginCommand autoRestart = getCommand("autorestart");
            autoRestart.setExecutor(ar);
            // DeferRestart
            DeferRestart dr = new DeferRestart(this);
            PluginCommand deferRestart = getCommand("deferrestart");
            deferRestart.setExecutor(dr);
            // WorldControl
            WorldControl worldControl = new WorldControl(this);
            PluginCommand worldControlCommand = getCommand("worldcontrol");
            worldControlCommand.setExecutor(worldControl);
            // DoubleXP
            doubleEXP DoubleEXp = new doubleEXP(this);
            PluginCommand doubleEXPCommand = getCommand("doublexp");
            doubleEXPCommand.setExecutor(DoubleEXp);

            pluginLogger.info("VoxelSMP commands loaded successfully");
            pluginLogger.info("Loading VoxelSMP tab completions");
            AutoComplete tabCompleter = new AutoComplete();
            gpCommand.setTabCompleter(tabCompleter);
            // noNether
            AutoCompleteNether tabCompleterNether = new AutoCompleteNether();
            worldControlCommand.setTabCompleter(tabCompleterNether);

            pluginLogger.info("VoxelSMP tab completions loaded successfully");
            pluginLogger.info("Loading VoxelSMP events");
            // Hits
            getServer().getPluginManager().registerEvents(new HandlePlayerHit(this), this);
            // Ban Feather
            getServer().getPluginManager().registerEvents(new BanFeather(), this);
            //No grief
            getServer().getPluginManager().registerEvents(new NoGriefDuringGP(this), this);
            // DoubleEXP Listener
            getServer().getPluginManager().registerEvents(new EXPListener(this), this);
            pluginLogger.info("VoxelSMP events loaded successfully");
            pluginLogger.info("Loading VoxelSMP features");
            // Lifesteal
            lifesteal = new Lifesteal(this);
            getServer().getPluginManager().registerEvents(lifesteal, this);
            // PurgeDay
            getServer().getPluginManager().registerEvents(new PurgeDay(this, lifesteal), this);

            pluginLogger.info("VoxelSMP features loaded successfully");
            pluginLogger.info("VoxelSMP core started successfully in " + (System.currentTimeMillis() - StartupTime) + "ms");
        }
        catch (NullPointerException e) {
            pluginLogger.severe("Failed to initialize VoxelSMP Core. Reason: " + e.getCause());
        }


    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (gracePeriodActive) {
            gracePeriodBossBar.setVisible(false);
        }
        if (gracePeriodUpdateTask != null) {
            gracePeriodUpdateTask.cancel();
        }
        Bukkit.getLogger().info("Stopping VoxelSMP core");
        saveConfig();
    }

    public boolean isDoubleEXPEnabled() {
        return isDoubleEXPEnabled;
    }

    public void setDoubleEXPEnabled(boolean enabled) {
        isDoubleEXPEnabled = enabled;
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
