package dev.portalgenesis.rrSubs;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class RrSubs extends JavaPlugin {

    private SubscriptionManager subscriptionManager;
    private ConfigManager configManager;
    private BukkitRunnable autosaveTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        configManager = new ConfigManager(this);
        subscriptionManager = new SubscriptionManager(this);

        int interval = configManager.getAutosaveInterval();
        autosaveTask = new BukkitRunnable() {
            @Override
            public void run() {
                subscriptionManager.save();
            }
        };

        autosaveTask.runTaskTimerAsynchronously(this, interval * 20L, interval * 20L);
        new RRSubsCommand(subscriptionManager, configManager, this); // регистрация команд
    }

    @Override
    public void onDisable() {
        if (autosaveTask != null) autosaveTask.cancel();
        subscriptionManager.save();
    }

    public SubscriptionManager getSubscriptionManager() {
        return subscriptionManager;
    }
}