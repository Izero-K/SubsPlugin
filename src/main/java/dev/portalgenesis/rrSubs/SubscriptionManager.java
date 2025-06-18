package dev.portalgenesis.rrSubs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SubscriptionManager {
    private final File dataFile;
    private final Map<String, SubscriptionType> subscriptions;
    private final Gson gson;
    private final JavaPlugin plugin;
    private final Logger logger;
    private final ConfigManager cfg;

    public SubscriptionManager(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.cfg = configManager;
        this.logger = plugin.getSLF4JLogger();
        this.dataFile = new File(plugin.getDataFolder(), "subscriptions.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.subscriptions = new ConcurrentHashMap<>();

        if (!plugin.getDataFolder().exists()) {
            if (!plugin.getDataFolder().mkdirs()) {
                logger.error("Failed to mkdir plugin data folder");
            }
        }

        load();
    }

    public void setSubscription(String playerName, SubscriptionType type) {
        subscriptions.put(playerName.toLowerCase(), type);
        save();

        Player player = Bukkit.getPlayer(playerName);
        if (player != null) {
            player.sendMessage(cfg.getMessage("sub_changed_notify", type.getDisplayName()));
        }
    }

    public SubscriptionType getSubscription(String playerName) {
        return subscriptions.getOrDefault(playerName.toLowerCase(), SubscriptionType.NONE);
    }

    public boolean hasSubscription(Player player, SubscriptionType type) {
        return type.equals(getSubscription(player.getName()));
    }

    public void removeSubscription(String playerName) {
        subscriptions.remove(playerName.toLowerCase());
        save();
    }

    public Map<String, SubscriptionType> getAllSubscriptions() {
        return Map.copyOf(subscriptions);
    }

    public void save() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Writer writer = new FileWriter(dataFile)) {
                gson.toJson(subscriptions, writer);
            } catch (IOException e) {
                logger.error("Failed to save subscriptions", e);
            }
        });
    }

    public void load() {
        if (!dataFile.exists()) return;

        try (Reader reader = new FileReader(dataFile)) {
            Type type = new TypeToken<Map<String, SubscriptionType>>() {
            }.getType();
            Map<String, SubscriptionType> loaded = gson.fromJson(reader, type);

            if (loaded != null) {
                subscriptions.clear();
                loaded.forEach((name, sub) -> subscriptions.put(name.toLowerCase(), sub));
            }
        } catch (IOException e) {
            logger.error("Failed to load subscriptions", e);
        }
    }
}