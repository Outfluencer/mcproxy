package dev.outfluencer.mcproxy.proxy.connection.handler.common;

import dev.outfluencer.mcproxy.networking.protocol.packets.common.ClientboundCommonDisconnectPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.common.ClientboundCommonPacketListener;
import dev.outfluencer.mcproxy.proxy.connection.ServerImpl;
import lombok.RequiredArgsConstructor;

import java.net.InetSocketAddress;

@RequiredArgsConstructor
public class ClientboundCommonPacketListenerImpl implements ClientboundCommonPacketListener {

    protected final ServerImpl server;

    @Override
    public boolean handle(ClientboundCommonDisconnectPacket packet) {
        server.getConnection().secureClose(null);
        return false;
    }


    @Override
    public void onDisconnect() {
        if (server.getPlayer().getServer() != server) return;
        server.getPlayer().connect(new InetSocketAddress("127.0.0.1", 25566));
    }
}
