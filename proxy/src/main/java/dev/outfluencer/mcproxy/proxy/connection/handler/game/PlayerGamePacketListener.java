package dev.outfluencer.mcproxy.proxy.connection.handler.game;

import dev.outfluencer.mcproxy.api.command.CommandManager;
import dev.outfluencer.mcproxy.networking.protocol.packets.game.ClientboundCommandSuggestionsPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.game.ServerboundChatCommandPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.game.ServerboundCommandSuggestionPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.game.ServerboundConfigurationAcknowledgedPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.game.ServerboundGamePacketListener;
import dev.outfluencer.mcproxy.networking.protocol.packets.login.ServerboundLoginAcknowledgedPacket;
import dev.outfluencer.mcproxy.networking.protocol.registry.Protocol;
import dev.outfluencer.mcproxy.proxy.MinecraftProxy;
import dev.outfluencer.mcproxy.proxy.connection.PlayerImpl;
import dev.outfluencer.mcproxy.proxy.connection.handler.common.PlayerCommonPacketListener;
import dev.outfluencer.mcproxy.proxy.connection.handler.config.PlayerConfigurationPacketListener;
import net.lenni0451.mcstructs.text.TextComponent;

public class PlayerGamePacketListener extends PlayerCommonPacketListener implements ServerboundGamePacketListener {

    public PlayerGamePacketListener(PlayerImpl player) {
        assert player.getDecoderProtocol() == Protocol.GAME;
        super(player);
    }

    @Override
    public boolean handle(ServerboundConfigurationAcknowledgedPacket packet) {
        player.getConnection().setPacketListener(new PlayerConfigurationPacketListener(player));
        if (!getServer().getConfigurationTracker().isPendingStartConfigAck()) {
            return DROP;
        }
        getServer().sendPacket(getServer().getEncoderProtocol() == Protocol.LOGIN ? new ServerboundLoginAcknowledgedPacket() : packet);
        return DROP;
    }

    @Override
    public boolean handle(ServerboundChatCommandPacket packet) {
        if (MinecraftProxy.getInstance().getCommandManager().execute(packet.getMessage(), player)) {
            return DROP;
        }
        return PASS;
    }

    @Override
    public boolean handle(ServerboundCommandSuggestionPacket packet) {
        CommandManager commandManager = MinecraftProxy.getInstance().getCommandManager();
        String command = packet.getCommand();

        // strip leading slash if present
        if (command.startsWith("/")) {
            command = command.substring(1);
        }

        // check if the input matches a proxy command
        String rootCommand = command.split(" ", 2)[0];
        if (commandManager.getDispatcher().getRoot().getChild(rootCommand) == null) {
            return PASS;
        }

        commandManager.tabComplete(command, player).thenAccept(suggestions -> {
            if (suggestions.isEmpty()) {
                return;
            }

            ClientboundCommandSuggestionsPacket.Entry[] entries = suggestions.getList().stream().map(s -> new ClientboundCommandSuggestionsPacket.Entry(s.getText(), s.getTooltip() != null ? TextComponent.of(s.getTooltip().getString()) : null)).toArray(ClientboundCommandSuggestionsPacket.Entry[]::new);

            player.sendPacket(new ClientboundCommandSuggestionsPacket(packet.getId(), suggestions.getRange().getStart() + 1, suggestions.getRange().getLength(), entries));
        });
        return DROP;
    }
}
