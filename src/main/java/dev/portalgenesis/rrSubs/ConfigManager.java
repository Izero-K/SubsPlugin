package dev.portalgenesis.rrSubs;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {

    private final JavaPlugin plugin;
    private final FileConfiguration config;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    public int getAutosaveInterval() {
        return config.getInt("autosave_interval", 20); // по умолчанию 20 секунд
    }

    public String getMessage(String key) {
        return config.getString("messages." + key, "§c[!] Сообщение не найдено: " + key);
    }

    public void reload() {
        plugin.reloadConfig();
    }
}