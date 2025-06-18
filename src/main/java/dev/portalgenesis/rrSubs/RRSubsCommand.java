package dev.portalgenesis.rrSubs;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.OfflinePlayer;

import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

public class RRSubsCommand {
    private final SubscriptionManager subscriptionManager;
    private final ConfigManager configManager;
    private final JavaPlugin plugin;
    private final String[] subscriptionTypeNames;

    public RRSubsCommand(SubscriptionManager subscriptionManager, ConfigManager configManager, JavaPlugin plugin) {
        this.subscriptionManager = subscriptionManager;
        this.configManager = configManager;
        this.plugin = plugin;
        this.subscriptionTypeNames = Arrays.stream(SubscriptionType.values())
                .map(SubscriptionType::getCleanName)
                .toArray(String[]::new);
        registerCommands();
    }

    private void registerCommands() {
        new CommandAPICommand("rrsubs")
                .withPermission("rrsubs.admin")
                .withAliases("rrs")
                .withSubcommands(
                        createGiveCommand(),
                        createSeeCommand(),
                        createReloadCommand(),
                        createListCommand(),
                        createListPlayersCommand()
                )
                .register();
    }

    private CommandAPICommand createGiveCommand() {
        return new CommandAPICommand("give")
                .withArguments(new PlayerArgument("player"))
                .withArguments(new StringArgument("type")
                        .replaceSuggestions(ArgumentSuggestions.strings(subscriptionTypeNames)))
                .executes((sender, args) -> {
                    Player target = (Player) args.get("player");
                    String inputType = (String) args.get("type");

                    // Находим соответствующий enum по чистому имени
                    SubscriptionType type = Arrays.stream(SubscriptionType.values())
                            .filter(st -> st.getCleanName().equalsIgnoreCase(inputType))
                            .findFirst()
                            .orElse(null);

                    if (type == null) {
                        sendError(sender, "Неверный тип подписки. Доступные: " +
                                String.join(", ", subscriptionTypeNames));
                        return;
                    }

                    subscriptionManager.setSubscription(target.getName(), type);
                    sendSuccess(sender, "Подписка '" + type.getDisplayName() +
                            "' выдана игроку " + target.getName());
                });
    }

    private CommandAPICommand createSeeCommand() {
        return new CommandAPICommand("see")
                .withArguments(new PlayerArgument("player"))
                .executes((sender, args) -> {
                    Player target = (Player) args.get("player");
                    SubscriptionType type = subscriptionManager.getSubscription(target.getName());
                    sendInfo(sender, "§6Игрок §e" + target.getName() + "§6 имеет подписку: " +
                            type.getDisplayName());
                });
    }

    private CommandAPICommand createListPlayersCommand() {
        return new CommandAPICommand("listplayers")
                .withAliases("lp", "players")
                .executes((sender, args) -> {
                    Map<String, SubscriptionType> subscriptions = subscriptionManager.getAllSubscriptions();

                    if (subscriptions == null || subscriptions.isEmpty()) {
                        sendInfo(sender, "§eНет игроков с активными подписками");
                        return;
                    }

                    StringBuilder message = new StringBuilder();
                    message.append("§6╔══════════════§f[ §bСписок подписок §f]§6══════════════╗\n");

                    subscriptions.forEach((name, type) -> {
                        boolean isOnline = Bukkit.getPlayer(name) != null;
                        String status = isOnline ? "§a● Онлайн" : "§7● Оффлайн";
                        message.append(String.format("§6│ §e%-16s §8%s §f│ §b%-12s §6│\n",
                                name,
                                status,
                                type.getDisplayName()));
                    });

                    message.append("§6╚══════════════════════════════════════════════╝");
                    sender.sendMessage(message.toString());
                });
    }

    private CommandAPICommand createReloadCommand() {
        return new CommandAPICommand("reload")
                .withPermission("rrsubs.admin.reload")
                .executes((sender, args) -> {
                    plugin.reloadConfig();
                    configManager.reload();
                    sendSuccess(sender, "§aКонфигурация успешно перезагружена!");
                    plugin.getLogger().info("Конфигурация перезагружена администратором " + sender.getName());
                });
    }

    private CommandAPICommand createListCommand() {
        return new CommandAPICommand("list")
                .executes((sender, args) -> {
                    String formattedTypes = String.join("§f, §e", subscriptionTypeNames);
                    sendInfo(sender, "§6Доступные типы подписок: §e" + formattedTypes + "§f");
                });
    }

    private void sendSuccess(CommandSender sender, String message) {
        sender.sendMessage("§a" + message);
    }

    private void sendError(CommandSender sender, String message) {
        sender.sendMessage("§c" + message);
    }

    private void sendInfo(CommandSender sender, String message) {
        sender.sendMessage("§e" + message);
    }
}