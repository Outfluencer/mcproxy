package dev.outfluencer.mcproxy.proxy.command;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import dev.outfluencer.mcproxy.api.ProxyServer;
import dev.outfluencer.mcproxy.api.ServerInfo;
import dev.outfluencer.mcproxy.api.command.CommandManager;
import dev.outfluencer.mcproxy.api.command.CommandSource;
import dev.outfluencer.mcproxy.api.connection.Player;
import dev.outfluencer.mcproxy.api.util.ComponentBuilder;
import dev.outfluencer.mcproxy.proxy.MinecraftProxy;
import dev.outfluencer.mcproxy.proxy.connection.PlayerImpl;
import net.lenni0451.mcstructs.text.TextComponent;

import java.awt.*;
import java.util.Collection;
import java.util.List;

public final class ProxyCommands {

    private ProxyCommands() {
    }

    public static void register(CommandManager manager) {
        manager.register(end());
        manager.register(list());
        manager.register(server());
        manager.register(send());
        manager.register(mcproxy());
    }

    private static LiteralArgumentBuilder<CommandSource> mcproxy() {
        TextComponent message = ComponentBuilder.gradient("This server is running " + ProxyServer.getInstance().getName() + " " + ProxyServer.getInstance().getVersion() + " by Outfluencer", new Color(30, 166, 120), new Color(55, 200, 219)).build();
        return LiteralArgumentBuilder.<CommandSource>literal("mcproxy").executes(ctx -> {
            ctx.getSource().sendMessage(message);
            return 1;
        });
    }

    private static LiteralArgumentBuilder<CommandSource> end() {
        return LiteralArgumentBuilder.<CommandSource>literal("end").requires(sender -> sender.hasPermission("mcproxy.command.end")).executes(ctx -> {
            MinecraftProxy.getInstance().stop(null);
            return 1;
        });
    }

    private static LiteralArgumentBuilder<CommandSource> list() {
        return LiteralArgumentBuilder.<CommandSource>literal("glist").requires(sender -> sender.hasPermission("mcproxy.command.glist")).executes(ctx -> {
            MinecraftProxy proxy = MinecraftProxy.getInstance();
            Collection<Player> players = proxy.getPlayers();
            ctx.getSource().sendMessage("Players (" + players.size() + "): " + String.join(", ", players.stream().map(Player::getName).toList()));
            return 1;
        });
    }

    private static LiteralArgumentBuilder<CommandSource> server() {
        return LiteralArgumentBuilder.<CommandSource>literal("server").requires(sender -> sender.hasPermission("mcproxy.command.server")).executes(ctx -> {
            MinecraftProxy proxy = MinecraftProxy.getInstance();
            StringBuilder sb = new StringBuilder("Servers: ");
            for (ServerInfo server : proxy.getConfig().getServers()) {
                sb.append(server.getName()).append(" ");
            }
            ctx.getSource().sendMessage(sb.toString().trim());
            return 1;
        }).then(RequiredArgumentBuilder.<CommandSource, String>argument("server", StringArgumentType.word()).suggests((ctx, builder) -> {
            for (ServerInfo server : MinecraftProxy.getInstance().getConfig().getServers()) {
                if (server.getName().toLowerCase().startsWith(builder.getRemainingLowerCase())) {
                    builder.suggest(server.getName());
                }
            }
            return builder.buildFuture();
        }).executes(ctx -> {
            CommandSource source = ctx.getSource();
            if (!(source instanceof Player player)) {
                source.sendMessage("This command can only be used by players.");
                return 0;
            }
            String serverName = StringArgumentType.getString(ctx, "server");
            ServerInfo serverInfo = MinecraftProxy.getInstance().getConfig().getServer(serverName);
            if (serverInfo == null) {
                source.sendMessage("Server '" + serverName + "' not found.");
                return 0;
            }
            player.connect(serverInfo);
            source.sendMessage("Connecting to " + serverName + "...");
            return 1;
        }));
    }

    private static LiteralArgumentBuilder<CommandSource> send() {
        return LiteralArgumentBuilder.<CommandSource>literal("send").requires(sender -> sender.hasPermission("mcproxy.command.send")).then(RequiredArgumentBuilder.<CommandSource, String>argument("player", StringArgumentType.word()).suggests((ctx, builder) -> {
            String remaining = builder.getRemainingLowerCase();
            if ("-all".startsWith(remaining)) {
                builder.suggest("-all", () -> "All players");
            }
            if ("-current".startsWith(remaining)) {
                builder.suggest("-server", () -> "All players on your server");
            }
            for (Player player : MinecraftProxy.getInstance().getPlayers()) {
                if (player.getName().toLowerCase().startsWith(remaining)) {
                    builder.suggest(player.getName());
                }
            }
            return builder.buildFuture();
        }).then(RequiredArgumentBuilder.<CommandSource, String>argument("server", StringArgumentType.word()).suggests((ctx, builder) -> {
            for (ServerInfo server : MinecraftProxy.getInstance().getConfig().getServers()) {
                if (server.getName().toLowerCase().startsWith(builder.getRemainingLowerCase())) {
                    builder.suggest(server.getName());
                }
            }
            return builder.buildFuture();
        }).executes(ctx -> {
            CommandSource source = ctx.getSource();
            MinecraftProxy proxy = MinecraftProxy.getInstance();
            String playerName = StringArgumentType.getString(ctx, "player");
            String serverName = StringArgumentType.getString(ctx, "server");

            ServerInfo serverInfo = proxy.getConfig().getServer(serverName);
            if (serverInfo == null) {
                source.sendMessage("Server '" + serverName + "' not found.");
                return 0;
            }

            Collection<? extends Player> targets;
            if (playerName.equals("-all")) {
                targets = proxy.getPlayers();
            } else if (playerName.equals("-current")) {
                if (!(source instanceof Player sender)) {
                    source.sendMessage("-current can only be used by players.");
                    return 0;
                }
                ServerInfo currentServer = ((PlayerImpl) sender).getServer().getServerInfo();
                targets = proxy.getPlayers().stream()
                        .filter(p -> ((PlayerImpl) p).getServer() != null
                                && ((PlayerImpl) p).getServer().getServerInfo().equals(currentServer))
                        .toList();
            } else {
                PlayerImpl target = proxy.getPlayer(playerName);
                if (target == null) {
                    source.sendMessage("Player '" + playerName + "' not found.");
                    return 0;
                }
                targets = List.of(target);
            }

            for (Player target : targets) {
                target.connect(serverInfo);
            }
            source.sendMessage("Sending " + targets.size() + " player(s) to " + serverName + "...");
            return 1;
        })));
    }
}
