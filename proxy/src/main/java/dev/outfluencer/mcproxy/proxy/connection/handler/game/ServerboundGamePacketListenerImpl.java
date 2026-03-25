package dev.outfluencer.mcproxy.proxy.connection.handler.game;

import dev.outfluencer.mcproxy.networking.protocol.packets.game.ServerboundConfigurationAcknowledgedPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.game.ServerboundGamePacketListener;
import dev.outfluencer.mcproxy.networking.protocol.packets.login.ServerboundLoginAcknowledgedPacket;
import dev.outfluencer.mcproxy.networking.protocol.registry.Protocol;
import dev.outfluencer.mcproxy.proxy.connection.PlayerImpl;
import dev.outfluencer.mcproxy.proxy.connection.handler.common.ServerboundCommonPacketListenerImpl;
import dev.outfluencer.mcproxy.proxy.connection.handler.config.ServerboundConfigurationPacketListenerImpl;

public class ServerboundGamePacketListenerImpl extends ServerboundCommonPacketListenerImpl implements ServerboundGamePacketListener {

    public ServerboundGamePacketListenerImpl(PlayerImpl player) {
        super(player);
    }

    @Override
    public boolean handle(ServerboundConfigurationAcknowledgedPacket packet) {
        player.setDecoderProtocol(Protocol.CONFIG);
        player.getConnection().setPacketListener(new ServerboundConfigurationPacketListenerImpl(player));

        Protocol protocol = player.getServer().getEncoderProtocol();
        player.getServer().sendPacket(protocol == Protocol.LOGIN ? new ServerboundLoginAcknowledgedPacket() : packet);
        player.getServer().setEncoderProtocol(Protocol.CONFIG);
        return false;

    }
}
