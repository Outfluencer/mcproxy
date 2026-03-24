package dev.outfluencer.mcproxy.proxy.connection.handler.config;

import dev.outfluencer.mcproxy.networking.protocol.DecodedPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.config.ClientboundConfigurationPacketListener;
import dev.outfluencer.mcproxy.networking.protocol.packets.config.ClientboundFinishConfigurationPacket;
import dev.outfluencer.mcproxy.networking.protocol.registry.Protocol;
import dev.outfluencer.mcproxy.proxy.connection.ServerImpl;
import dev.outfluencer.mcproxy.proxy.connection.handler.game.ClientboundGamePacketListenerImpl;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ClientboundConfigurationPacketListenerImpl implements ClientboundConfigurationPacketListener {

    private final ServerImpl server;

    @Override
    public void handle(DecodedPacket decodedPacket) {
        server.getPlayer().getConnection().sendDecodedPacket(decodedPacket);
    }

    @Override
    public boolean handle(ClientboundFinishConfigurationPacket packet) {
        server.getConnection().setDecoderProtocol(Protocol.GAME);
        server.getConnection().setPacketListener(new ClientboundGamePacketListenerImpl(server));

        server.getPlayer().getConnection().sendPacket(packet);
        server.getPlayer().getConnection().setEncoderProtocol(Protocol.GAME);
        return false;
    }
}
