package dev.outfluencer.mcproxy.proxy.connection.handler.common;

import dev.outfluencer.mcproxy.api.ProxyServer;
import dev.outfluencer.mcproxy.api.ServerInfo;
import dev.outfluencer.mcproxy.api.events.ServerKickPlayerEvent;
import dev.outfluencer.mcproxy.networking.protocol.packets.common.ClientboundCommonDisconnectPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.common.ClientboundCommonPacketListener;
import dev.outfluencer.mcproxy.networking.protocol.packets.common.ClientboundUpdateTagsPacket;
import dev.outfluencer.mcproxy.networking.protocol.registry.Protocol;
import dev.outfluencer.mcproxy.proxy.connection.PlayerImpl;
import dev.outfluencer.mcproxy.proxy.connection.ServerImpl;

public class ServerCommonPacketListener implements ClientboundCommonPacketListener {

    protected final ServerImpl server;
    protected final PlayerImpl player;

    public ServerCommonPacketListener(ServerImpl server) {
        this.server = server;
        this.player = server.getPlayer();
    }

    @Override
    public boolean handle(ClientboundCommonDisconnectPacket packet) {
        try {
            var event = ProxyServer.getInstance().getEventManager().fire(new ServerKickPlayerEvent(player, server, packet.getReason()));
            event.setCancelled(true);
            if (event.isCancelled()) {
                if (event.getFallbackServer() != null) {
                    server.setDiscarded(true);
                    player.connect(new ServerInfo());
                }
                return DROP;
            }
            packet.setReason(event.getReason());
            player.sendPacket(packet);
            return DROP;
        } finally {
            server.disconnect(packet.getReason());
        }
    }

    @Override
    public boolean handle(ClientboundUpdateTagsPacket clientboundUpdateTagsPacket) {
        throw new UnsupportedOperationException("Only registered in config phase");
    }

    @Override
    public void onDisconnect() {
        if(server.isDiscarded()) {
            return;
        }
        server.setDiscarded(true);
        if (player.getServer() != server) {
            // player already on another server
            return;
        }
        // connect to next fallback server.
        player.fallback();
    }

    @Override
    public String toString() {
        return "[" + getClass().getSimpleName() + "|" + player.getName() + "|" + server.getName() + "]";
    }
}
