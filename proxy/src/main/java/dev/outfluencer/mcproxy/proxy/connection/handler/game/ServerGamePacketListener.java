package dev.outfluencer.mcproxy.proxy.connection.handler.game;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.outfluencer.mcproxy.api.command.CommandSource;
import dev.outfluencer.mcproxy.networking.protocol.DecodedPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.game.ClientboundBundleDelimiterPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.game.ClientboundCommandsPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.game.ClientboundCommandsPacket.CompletionProviders;
import dev.outfluencer.mcproxy.networking.protocol.packets.game.ClientboundCommandSuggestionsPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.game.ClientboundGamePacketListener;
import dev.outfluencer.mcproxy.networking.protocol.packets.game.ClientboundRespawnPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.game.ClientboundStartConfigurationPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.game.ClientboundSystemChatPacket;
import dev.outfluencer.mcproxy.networking.protocol.registry.Protocol;
import dev.outfluencer.mcproxy.proxy.MinecraftProxy;
import dev.outfluencer.mcproxy.proxy.connection.ServerImpl;
import dev.outfluencer.mcproxy.proxy.connection.handler.common.ServerCommonPacketListener;
import dev.outfluencer.mcproxy.proxy.connection.handler.config.ServerConfigurationPacketListener;

public class ServerGamePacketListener extends ServerCommonPacketListener implements ClientboundGamePacketListener {

    private final MinecraftProxy proxy = MinecraftProxy.getInstance();

    public ServerGamePacketListener(ServerImpl server) {
        assert server.getDecoderProtocol() == Protocol.GAME;
        super(server);
    }

    @Override
    public boolean handle(ClientboundStartConfigurationPacket packet) {
        server.getConnection().setPacketListener(new ServerConfigurationPacketListener(server));
        server.getConfigurationTracker().setPendingStartConfigAck(true);
        return PASS;
    }

    @Override
    public boolean handle(ClientboundBundleDelimiterPacket clientboundBundleDelimiterPacket) {
        player.toggleBundle();
        return PASS;
    }

    @Override
    public boolean handle(ClientboundSystemChatPacket clientboundSystemChatPacket) {
        return PASS;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public boolean handle(ClientboundCommandsPacket commands) {
        boolean modified = false;

        for (CommandNode<CommandSource> proxyCommand : proxy.getCommandManager().getDispatcher().getRoot().getChildren()) {
            // skip commands the player doesn't have permission for
            if (!proxyCommand.canUse(player)) continue;

            // remove existing server command with the same name
            CommandNode existing = commands.getRoot().getChild(proxyCommand.getName());
            if (existing != null) {
                commands.getRoot().getChildren().remove(existing);
            }

            // rebuild the node tree with ASK_SERVER suggestions so it can be serialized
            commands.getRoot().addChild(toWireNode(proxyCommand));
            modified = true;
        }

        if (modified) {
            player.sendPacket(commands);
            return DROP;
        }
        return PASS;
    }

    private static final com.mojang.brigadier.Command NOOP_COMMAND = _ -> 0;

    /**
     * Deep-copies a command node tree for the wire, replacing custom SuggestionProviders
     * with ASK_SERVER so the client delegates all suggestions to the proxy.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static CommandNode toWireNode(CommandNode<CommandSource> source) {
        ArgumentBuilder builder;

        if (source instanceof LiteralCommandNode literal) {
            builder = LiteralArgumentBuilder.literal(literal.getLiteral());
        } else if (source instanceof ArgumentCommandNode argNode) {
            RequiredArgumentBuilder argBuilder = RequiredArgumentBuilder.argument(argNode.getName(), argNode.getType());
            if (argNode.getCustomSuggestions() != null) {
                argBuilder.suggests(CompletionProviders.ASK_SERVER);
            }
            builder = argBuilder;
        } else {
            return source;
        }

        builder.executes(NOOP_COMMAND);
        if (source.getRedirect() != null) {
            builder.redirect(toWireNode(source.getRedirect()));
        }

        for (CommandNode<CommandSource> child : source.getChildren()) {
            builder.then(toWireNode(child));
        }

        return builder.build();
    }

    @Override
    public boolean handle(ClientboundCommandSuggestionsPacket packet) {
        return PASS;
    }

    @Override
    public boolean handle(ClientboundRespawnPacket clientboundRespawnDelimiterPacket) {
        return PASS;
    }

    @Override
    public void handle(DecodedPacket decodedPacket) {
        player.sendDecodedPacket(decodedPacket);
    }
}
