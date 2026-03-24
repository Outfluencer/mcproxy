package dev.outfluencer.mcproxy.proxy.connection.handler.game;

import dev.outfluencer.mcproxy.networking.ConnectionHandle;
import dev.outfluencer.mcproxy.networking.protocol.DecodedPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.game.ServerboundConfigurationAcknowledgedPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.game.ServerboundGamePacketListener;
import dev.outfluencer.mcproxy.networking.protocol.packets.login.ServerboundLoginAcknowledgedPacket;
import dev.outfluencer.mcproxy.networking.protocol.registry.Protocol;
import dev.outfluencer.mcproxy.proxy.connection.PlayerImpl;
import dev.outfluencer.mcproxy.proxy.connection.ServerImpl;
import dev.outfluencer.mcproxy.proxy.connection.handler.config.ServerboundConfigurationPacketListenerImpl;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ServerboundGamePacketListenerImpl implements ServerboundGamePacketListener {

    private final PlayerImpl player;

    @Override
    public boolean handle(ServerboundConfigurationAcknowledgedPacket packet) {
        System.out.println("AAAAAAAA");
        player.getConnection().setDecoderProtocol(Protocol.CONFIG);
        player.getConnection().setPacketListener(new ServerboundConfigurationPacketListenerImpl(player));

        Protocol protocol = player.getServer().getConnection().getEncoderProtocol();
        player.getServer().getConnection().sendPacket(
            protocol == Protocol.LOGIN ?
            new ServerboundLoginAcknowledgedPacket() : packet
        );
        player.getServer().getConnection().setEncoderProtocol(Protocol.CONFIG);
        return false;

    }



    @Override
    public void handle(DecodedPacket decodedPacket) {
        ServerImpl server = player.getServer();
        ConnectionHandle serverConnection = server.getConnection();
        if (serverConnection.isClosed() || serverConnection.getDecoderProtocol() != decodedPacket.protocol()) {
            System.out.println("a: " + decodedPacket);
            return;
        }
        serverConnection.sendDecodedPacket(decodedPacket);
    }

    @Override
    public void onDisconnect() {
        player.getServer().getConnection().secureClose(null);
    }
}
