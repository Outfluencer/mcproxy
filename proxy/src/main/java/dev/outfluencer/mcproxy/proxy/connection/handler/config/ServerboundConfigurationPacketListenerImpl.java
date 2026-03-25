package dev.outfluencer.mcproxy.proxy.connection.handler.config;

import dev.outfluencer.mcproxy.networking.protocol.packets.config.ServerboundConfigurationPacketListener;
import dev.outfluencer.mcproxy.networking.protocol.packets.config.ServerboundFinishConfigurationPacket;
import dev.outfluencer.mcproxy.networking.protocol.registry.Protocol;
import dev.outfluencer.mcproxy.proxy.connection.PlayerImpl;
import dev.outfluencer.mcproxy.proxy.connection.handler.common.ServerboundCommonPacketListenerImpl;
import dev.outfluencer.mcproxy.proxy.connection.handler.game.ServerboundGamePacketListenerImpl;

public class ServerboundConfigurationPacketListenerImpl extends ServerboundCommonPacketListenerImpl implements ServerboundConfigurationPacketListener {

    public ServerboundConfigurationPacketListenerImpl(PlayerImpl player) {
        super(player);
    }

    @Override
    public boolean handle(ServerboundFinishConfigurationPacket packet) {
        player.setDecoderProtocol(Protocol.GAME);
        player.getConnection().setPacketListener(new ServerboundGamePacketListenerImpl(player));

        player.getServer().sendPacket(packet);
        player.getServer().setEncoderProtocol(Protocol.GAME);

        return false;
    }

}
