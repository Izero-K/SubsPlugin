package dev.portalgenesis.rrSubs;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.Map;

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
                .map(Enum::name)
                .toArray(String[]::new);

        registerCommands();
    }

    public void sendMessage(Player to, String key, Object... args) {
        to.sendMessage(configManager.getMessage(key, args));
    }

    private void registerCommands() {
        new CommandAPICommand("subs")
                .withPermission("rrsubs.admin")
                .withSubcommands(
                        createGiveCommand(),
                        createSeeCommand(),
                        createListCommand(),
                        createListPlayersCommand(),
                        createReloadCommand()
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

                    SubscriptionType type;
                    try {
                        type = SubscriptionType.valueOf(inputType.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        sendError(sender, "Неверный тип подписки. Доступные: " +
                                String.join(", ", subscriptionTypeNames));
                        return;
                    }

                    subscriptionManager.setSubscription(target.getName(), type);
                    sendSuccess(sender, "§aВыдана подписка '" + type.getDisplayName() +
                            "§a' игроку " + target.getName());
                });
    }

    private CommandAPICommand createSeeCommand() {
        return new CommandAPICommand("see")
                .withArguments(new PlayerArgument("player"))
                .executes((sender, args) -> {
                    Player target = (Player) args.get("player");
                    SubscriptionType type = subscriptionManager.getSubscription(target.getName());
                    sendInfo(sender, "§aУ игрока §e" + target.getName() + "§a подписка: " + type.getDisplayName());
                });
    }

    private CommandAPICommand createListCommand() {
        return new CommandAPICommand("list")
                .executes((sender, args) -> {
                    String types = Arrays.stream(SubscriptionType.values())
                            .map(SubscriptionType::name)
                            .collect(Collectors.joining("§f, §e"));
                    sendInfo(sender, "§aДоступные типы подписок: §e" + types);
                });
    }


    private CommandAPICommand createListPlayersCommand() {
        return new CommandAPICommand("listplayers")
                .executes((sender, args) -> {
                    Map<String, SubscriptionType> all = subscriptionManager.getAllSubscriptions();
                    if (all == null || all.isEmpty()) {
                        sendInfo(sender, "§eНет игроков с активными подписками");
                        return;
                    }

                    StringBuilder message = new StringBuilder();
                    message.append("§6╔═══════§f[ §bСписок подписок §f]§6═══════╗\n");

                    all.forEach((name, type) -> {
                        boolean isOnline = Bukkit.getPlayer(name) != null;
                        String status = isOnline ? "§a●" : "§7●";
                        String shortName = name.length() > 12 ? name.substring(0, 12) + "…" : name;
                        message.append(String.format("§6│ §e%-13s §8%s §6│ %s%-9s§r §6│\n",
                                shortName, status, type.getColor(), ""));
                    });

                    message.append("§6╚═════════════════════════╝");
                    sender.sendMessage(message.toString());


                });
    }

    private CommandAPICommand createReloadCommand() {
        return new CommandAPICommand("reload")
                .withPermission("rrsubs.admin.reload")
                .executes((sender, args) -> {
                    plugin.reloadConfig();
                    configManager.reload();
                    sendSuccess(sender, "Конфигурация перезагружена!");
                    plugin.getLogger().info("Конфигурация перезагружена " + sender.getName());
                });
    }

    private void sendSuccess(CommandSender sender, String msg) {
        sender.sendMessage("§a" + msg);
    }

    private void sendError(CommandSender sender, String msg) {
        sender.sendMessage("§c" + msg);
    }

    private void sendInfo(CommandSender sender, String msg) {
        sender.sendMessage("§e" + msg);
    }
}
