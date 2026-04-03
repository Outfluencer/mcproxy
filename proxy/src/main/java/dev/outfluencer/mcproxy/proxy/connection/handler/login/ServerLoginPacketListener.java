package dev.outfluencer.mcproxy.proxy.connection.handler.login;

import dev.outfluencer.mcproxy.api.ProxyServer;
import dev.outfluencer.mcproxy.api.events.CompressionChangeEvent;
import dev.outfluencer.mcproxy.api.ServerInfo;
import dev.outfluencer.mcproxy.networking.protocol.packets.game.ClientboundBundleDelimiterPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.game.ClientboundStartConfigurationPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.handshake.ServerboundHandshakePacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.login.ClientboundLoginCompressionPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.login.ClientboundLoginDisconnectPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.login.ClientboundLoginFinishedPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.login.ClientboundLoginPacketListener;
import dev.outfluencer.mcproxy.networking.protocol.packets.login.ServerboundHelloPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.login.ServerboundLoginAcknowledgedPacket;
import dev.outfluencer.mcproxy.networking.protocol.registry.Protocol;
import dev.outfluencer.mcproxy.proxy.connection.PlayerImpl;
import dev.outfluencer.mcproxy.proxy.connection.ServerImpl;
import dev.outfluencer.mcproxy.proxy.connection.handler.config.ServerConfigurationPacketListener;


public class ServerLoginPacketListener implements ClientboundLoginPacketListener {

    private final ProxyServer proxy = ProxyServer.getInstance();
    private final ServerImpl server;
    private final PlayerImpl player;

    public ServerLoginPacketListener(ServerImpl server) {
        this.server = server;
        this.player = server.getPlayer();
    }

    @Override
    public void onConnect() {
        ServerInfo serverInfo = server.getServerInfo();
        ServerboundHandshakePacket handshake = new ServerboundHandshakePacket(player.getConnection().getProtocolVersion(), serverInfo.getAddress(), serverInfo.getPort(), ServerboundHandshakePacket.ClientIntent.LOGIN);
        server.sendPacket(handshake);
        server.getConnection().setProtocol(Protocol.LOGIN);
        server.sendPacket(new ServerboundHelloPacket(player.getName(), player.getUuid()));
    }

    @Override
    public boolean handle(ClientboundLoginDisconnectPacket packet) {
        server.disconnect();
        return false;
    }

    @Override
    public boolean handle(ClientboundLoginFinishedPacket packet) {
        // get the server the player is currently connected to
        ServerImpl server = this.server.getPlayer().getServer();
        if (server != null) {
            if (server.isConnected()) {
                server.disconnect();
                server.getConnection()
                    .getChannel()
                    .closeFuture()
                    .addListener(_ -> updatePlayerServer(packet));
                return false;
            }
        }
        updatePlayerServer(packet);
        return false;
    }

    @Override
    public boolean handle(ClientboundLoginCompressionPacket packet) {
        server.getConnection().setCompression(packet.getThreshold());
        proxy.getEventManager().fire(new CompressionChangeEvent(packet.getThreshold(), server));
        return false;
    }

    public void updatePlayerServer(ClientboundLoginFinishedPacket packet) {
        if (!player.isConnected()) {
            return;
        }

        server.getConnection().setPacketListener(new ServerConfigurationPacketListener(server));
        player.setServer(server);

        Protocol playerEncoderProtocol = player.getEncoderProtocol();
        if (playerEncoderProtocol == Protocol.LOGIN) {
            player.sendPacket(packet);
        } else if (playerEncoderProtocol == Protocol.GAME) {
            if (player.isBundling()) {
                player.toggleBundle();
                player.sendPacket(new ClientboundBundleDelimiterPacket());
            }
            player.sendPacket(new ClientboundStartConfigurationPacket());
            server.getConfigurationTracker().pendingStartConfigAck = true;
        } else {
            assert playerEncoderProtocol == Protocol.CONFIG;
            server.sendPacket(new ServerboundLoginAcknowledgedPacket());
        }

    }

    @Override
    public String toString() {
        String name = player != null ? player.getName() : null;
        return "[" + getClass().getSimpleName() + "|" + (name != null ? name + "|" : "") + server.getConnection().getAddress() + "]";
    }

    @Override
    public void onDisconnect() {
        if (!player.isConnectedToServer()) {
            player.connectToNextFallback();
        }
    }
}
