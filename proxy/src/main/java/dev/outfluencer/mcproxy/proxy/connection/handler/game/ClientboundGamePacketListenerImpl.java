package dev.outfluencer.mcproxy.proxy.connection.handler.game;

import dev.outfluencer.mcproxy.networking.protocol.DecodedPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.game.ClientboundBundleDelimiterPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.game.ClientboundGamePacketListener;
import dev.outfluencer.mcproxy.networking.protocol.packets.game.ClientboundStartConfigurationPacket;
import dev.outfluencer.mcproxy.networking.protocol.registry.Protocol;
import dev.outfluencer.mcproxy.proxy.connection.ServerImpl;
import dev.outfluencer.mcproxy.proxy.connection.handler.common.ClientboundCommonPacketListenerImpl;
import dev.outfluencer.mcproxy.proxy.connection.handler.config.ClientboundConfigurationPacketListenerImpl;
import dev.outfluencer.mcproxy.proxy.connection.handler.config.ServerboundConfigurationPacketListenerImpl;

import java.net.InetSocketAddress;

public class ClientboundGamePacketListenerImpl extends ClientboundCommonPacketListenerImpl implements ClientboundGamePacketListener {

    public ClientboundGamePacketListenerImpl(ServerImpl server) {
        super(server);
    }

    @Override
    public boolean handle(ClientboundStartConfigurationPacket packet) {
        server.getConnection().sendPacket(packet);
        server.getConnection().setEncoderProtocol(Protocol.CONFIG);

        server.getPlayer().getConnection().setDecoderProtocol(Protocol.CONFIG);
        server.getPlayer().getConnection().setPacketListener(new ClientboundConfigurationPacketListenerImpl(server));
        return true;
    }

    @Override
    public boolean handle(ClientboundBundleDelimiterPacket clientboundBundleDelimiterPacket) {
        server.getPlayer().toggleBundle();
        return true;
    }

    @Override
    public void handle(DecodedPacket decodedPacket) {
        server.getPlayer().getConnection().sendDecodedPacket(decodedPacket);
    }

    @Override
    public void onDisconnect() {
        super.onDisconnect();
    }
}
