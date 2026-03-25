package dev.outfluencer.mcproxy.proxy.connection.handler.game;

import dev.outfluencer.mcproxy.networking.protocol.DecodedPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.game.ClientboundBundleDelimiterPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.game.ClientboundGamePacketListener;
import dev.outfluencer.mcproxy.networking.protocol.packets.game.ClientboundStartConfigurationPacket;
import dev.outfluencer.mcproxy.networking.protocol.registry.Protocol;
import dev.outfluencer.mcproxy.proxy.connection.ServerImpl;
import dev.outfluencer.mcproxy.proxy.connection.handler.common.ClientboundCommonPacketListenerImpl;
import dev.outfluencer.mcproxy.proxy.connection.handler.config.ClientboundConfigurationPacketListenerImpl;

public class ClientboundGamePacketListenerImpl extends ClientboundCommonPacketListenerImpl implements ClientboundGamePacketListener {

    public ClientboundGamePacketListenerImpl(ServerImpl server) {
        super(server);
    }

    @Override
    public boolean handle(ClientboundStartConfigurationPacket packet) {
        server.sendPacket(packet);
        server.setEncoderProtocol(Protocol.CONFIG);

        player.setDecoderProtocol(Protocol.CONFIG);
        player.getConnection().setPacketListener(new ClientboundConfigurationPacketListenerImpl(server));
        return true;
    }

    @Override
    public boolean handle(ClientboundBundleDelimiterPacket clientboundBundleDelimiterPacket) {
        player.toggleBundle();
        return true;
    }

    @Override
    public void handle(DecodedPacket decodedPacket) {
        player.sendDecodedPacket(decodedPacket);
    }

}
