package dev.outfluencer.mcproxy.proxy.connection.handler.common;

import dev.outfluencer.mcproxy.networking.ConnectionHandle;
import dev.outfluencer.mcproxy.networking.protocol.DecodedPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.common.ServerboundCommonPacketListener;
import dev.outfluencer.mcproxy.proxy.connection.PlayerImpl;
import dev.outfluencer.mcproxy.proxy.connection.ServerImpl;

public class PlayerCommonPacketListener implements ServerboundCommonPacketListener {

    protected final PlayerImpl player;
    protected final ConnectionHandle playerConnection;

    public PlayerCommonPacketListener(PlayerImpl player) {
        this.player = player;
        this.playerConnection = player.getConnection();
    }

    public ServerImpl getServer() {
        return player.getServer();
    }

    @Override
    public void handle(DecodedPacket decodedPacket) {
        ServerImpl server = getServer();
        if (!server.isConnected()) {
            return;
        }
        server.sendDecodedPacket(decodedPacket);
    }

    @Override
    public void onDisconnect() {
        // disconnect the backend server
        getServer().disconnect();
    }

    @Override
    public String toString() {
        return "[" + getClass().getSimpleName() + "|" + player.getName() + "|" + player.getAddress() + "]";
    }

}
