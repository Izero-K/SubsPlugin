package dev.portalgenesis.rrSubs;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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

    private static final LegacyComponentSerializer MESSAGE_PARSER = LegacyComponentSerializer.builder()
            .extractUrls()
            .character('&')
            .hexColors()
            .build();


    public Component getMessage(String key, Object... args) {
        var raw = config.getString(key, key);
        int i = 0;
        for (var arg : args) {
            raw = raw.replace("%"+i, String.valueOf(arg));
            i++;
        }
        return MESSAGE_PARSER.deserialize(raw);
    }

    public void reload() {
        plugin.reloadConfig();
    }
}