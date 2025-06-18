package dev.portalgenesis.rrSubs;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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

        CommandAPI.onEnable();
        new RRSubsCommand(subscriptionManager, configManager, this); // регистрация команд


    }

    @Override
    public void onDisable() {
        if (autosaveTask != null) autosaveTask.cancel();
        subscriptionManager.save();
    }


// CommandAPI

    public SubscriptionManager getSubscriptionManager() {
        return subscriptionManager;
    }
}