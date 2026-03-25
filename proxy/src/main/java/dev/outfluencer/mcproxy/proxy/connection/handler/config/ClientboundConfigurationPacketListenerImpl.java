package dev.outfluencer.mcproxy.proxy.connection.handler.config;

import dev.outfluencer.mcproxy.networking.protocol.DecodedPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.config.ClientboundConfigurationPacketListener;
import dev.outfluencer.mcproxy.networking.protocol.packets.config.ClientboundFinishConfigurationPacket;
import dev.outfluencer.mcproxy.networking.protocol.registry.Protocol;
import dev.outfluencer.mcproxy.proxy.connection.ServerImpl;
import dev.outfluencer.mcproxy.proxy.connection.handler.common.ClientboundCommonPacketListenerImpl;
import dev.outfluencer.mcproxy.proxy.connection.handler.game.ClientboundGamePacketListenerImpl;

public class ClientboundConfigurationPacketListenerImpl extends ClientboundCommonPacketListenerImpl implements ClientboundConfigurationPacketListener {

    public ClientboundConfigurationPacketListenerImpl(ServerImpl server) {
        super(server);
    }

    @Override
    public void handle(DecodedPacket decodedPacket) {
        player.sendDecodedPacket(decodedPacket);
    }

    @Override
    public boolean handle(ClientboundFinishConfigurationPacket packet) {
        server.setDecoderProtocol(Protocol.GAME);
        server.getConnection().setPacketListener(new ClientboundGamePacketListenerImpl(server));

        player.sendPacket(packet);
        player.setEncoderProtocol(Protocol.GAME);
        return false;
    }
}
