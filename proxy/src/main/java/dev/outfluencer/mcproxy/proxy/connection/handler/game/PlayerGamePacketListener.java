package dev.outfluencer.mcproxy.proxy.connection.handler.game;

import dev.outfluencer.mcproxy.networking.protocol.packets.game.ServerboundChatCommandPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.game.ServerboundConfigurationAcknowledgedPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.game.ServerboundGamePacketListener;
import dev.outfluencer.mcproxy.networking.protocol.packets.login.ServerboundLoginAcknowledgedPacket;
import dev.outfluencer.mcproxy.networking.protocol.registry.Protocol;
import dev.outfluencer.mcproxy.proxy.MinecraftProxy;
import dev.outfluencer.mcproxy.proxy.connection.PlayerImpl;
import dev.outfluencer.mcproxy.proxy.connection.handler.common.PlayerCommonPacketListener;
import dev.outfluencer.mcproxy.proxy.connection.handler.config.PlayerConfigurationPacketListener;

public class PlayerGamePacketListener extends PlayerCommonPacketListener implements ServerboundGamePacketListener {

    public PlayerGamePacketListener(PlayerImpl player) {
        assert player.getDecoderProtocol() == Protocol.GAME;
        super(player);
    }

    @Override
    public boolean handle(ServerboundConfigurationAcknowledgedPacket packet) {
        player.getConnection().setPacketListener(new PlayerConfigurationPacketListener(player));
        if (!getServer().getConfigurationTracker().pendingStartConfigAck) {
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
}
