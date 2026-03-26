package dev.outfluencer.mcproxy.proxy.connection.handler.game;

import dev.outfluencer.mcproxy.networking.protocol.packets.game.ServerboundConfigurationAcknowledgedPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.game.ServerboundGamePacketListener;
import dev.outfluencer.mcproxy.networking.protocol.packets.login.ServerboundLoginAcknowledgedPacket;
import dev.outfluencer.mcproxy.networking.protocol.registry.Protocol;
import dev.outfluencer.mcproxy.proxy.connection.PlayerImpl;
import dev.outfluencer.mcproxy.proxy.connection.handler.common.PlayerCommonPacketListener;
import dev.outfluencer.mcproxy.proxy.connection.handler.config.PlayerConfigurationPacketListener;

public class PlayerGamePacketListener extends PlayerCommonPacketListener implements ServerboundGamePacketListener {

    public PlayerGamePacketListener(PlayerImpl player) {
        super(player);
    }

    @Override
    public boolean handle(ServerboundConfigurationAcknowledgedPacket packet) {
        player.getConnection().setPacketListener(new PlayerConfigurationPacketListener(player));

        Protocol protocol = player.getServer().getEncoderProtocol();
        player.getServer().sendPacket(protocol == Protocol.LOGIN ? new ServerboundLoginAcknowledgedPacket() : packet);
        return false;

    }
}
