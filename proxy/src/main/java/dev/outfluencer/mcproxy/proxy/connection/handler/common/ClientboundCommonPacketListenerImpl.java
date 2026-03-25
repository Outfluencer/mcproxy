package dev.outfluencer.mcproxy.proxy.connection.handler.common;

import dev.outfluencer.mcproxy.networking.protocol.packets.common.ClientboundCommonDisconnectPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.common.ClientboundCommonPacketListener;
import dev.outfluencer.mcproxy.proxy.connection.PlayerImpl;
import dev.outfluencer.mcproxy.proxy.connection.ServerImpl;

public class ClientboundCommonPacketListenerImpl implements ClientboundCommonPacketListener {

    protected final ServerImpl server;
    protected final PlayerImpl player;

    public ClientboundCommonPacketListenerImpl(ServerImpl server) {
        this.server = server;
        this.player = server.getPlayer();
    }

    @Override
    public boolean handle(ClientboundCommonDisconnectPacket packet) {
        server.disconnect();
        return false;
    }


    @Override
    public void onDisconnect() {
        if (player.getServer() != server) {
            // player already on another server
            return;
        }
        // connect to next fallback server.
        player.fallback();
    }
}
