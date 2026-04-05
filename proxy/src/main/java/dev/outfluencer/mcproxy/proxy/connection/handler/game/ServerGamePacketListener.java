package dev.outfluencer.mcproxy.proxy.connection.handler.game;

import dev.outfluencer.mcproxy.networking.protocol.DecodedPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.game.ClientboundBundleDelimiterPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.game.ClientboundGamePacketListener;
import dev.outfluencer.mcproxy.networking.protocol.packets.game.ClientboundStartConfigurationPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.game.ClientboundSystemChatPacket;
import dev.outfluencer.mcproxy.networking.protocol.registry.Protocol;
import dev.outfluencer.mcproxy.proxy.connection.ServerImpl;
import dev.outfluencer.mcproxy.proxy.connection.handler.common.ServerCommonPacketListener;
import dev.outfluencer.mcproxy.proxy.connection.handler.config.ServerConfigurationPacketListener;

public class ServerGamePacketListener extends ServerCommonPacketListener implements ClientboundGamePacketListener {

    public ServerGamePacketListener(ServerImpl server) {
        assert server.getDecoderProtocol() == Protocol.GAME;
        super(server);
    }

    @Override
    public boolean handle(ClientboundStartConfigurationPacket packet) {
        server.getConnection().setPacketListener(new ServerConfigurationPacketListener(server));
        server.getConfigurationTracker().pendingStartConfigAck = true;
        return PASS;
    }

    @Override
    public boolean handle(ClientboundBundleDelimiterPacket clientboundBundleDelimiterPacket) {
        player.toggleBundle();
        return PASS;
    }

    @Override
    public boolean handle(ClientboundSystemChatPacket clientboundSystemChatPacket) {
        // forward
        return PASS;
    }

    @Override
    public void handle(DecodedPacket decodedPacket) {
        player.sendDecodedPacket(decodedPacket);
    }

}
