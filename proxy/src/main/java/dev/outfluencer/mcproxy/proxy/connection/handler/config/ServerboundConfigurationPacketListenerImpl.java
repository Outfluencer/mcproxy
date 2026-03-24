package dev.outfluencer.mcproxy.proxy.connection.handler.config;

import dev.outfluencer.mcproxy.networking.ConnectionHandle;
import dev.outfluencer.mcproxy.networking.protocol.DecodedPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.config.ServerboundConfigurationPacketListener;
import dev.outfluencer.mcproxy.networking.protocol.packets.config.ServerboundFinishConfigurationPacket;
import dev.outfluencer.mcproxy.networking.protocol.registry.Protocol;
import dev.outfluencer.mcproxy.proxy.connection.PlayerImpl;
import dev.outfluencer.mcproxy.proxy.connection.ServerImpl;
import dev.outfluencer.mcproxy.proxy.connection.handler.game.ServerboundGamePacketListenerImpl;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ServerboundConfigurationPacketListenerImpl implements ServerboundConfigurationPacketListener {

    private final PlayerImpl player;

    @Override
    public void handle(DecodedPacket decodedPacket) {
        ServerImpl server = player.getServer();
        ConnectionHandle serverConnection = server.getConnection();
        if (serverConnection.isClosed() || serverConnection.getDecoderProtocol() != decodedPacket.protocol()) {
            System.out.println("f: " + decodedPacket);
            return;
        }
        serverConnection.sendDecodedPacket(decodedPacket);
    }

    @Override
    public boolean handle(ServerboundFinishConfigurationPacket packet) {
        player.getConnection().setDecoderProtocol(Protocol.GAME);
        player.getConnection().setPacketListener(new ServerboundGamePacketListenerImpl(player));

        player.getServer().getConnection().sendPacket(packet);
        player.getServer().getConnection().setEncoderProtocol(Protocol.GAME);

        return false;
    }

    @Override
    public void onDisconnect() {
        player.getServer().getConnection().secureClose(null);
    }
}
