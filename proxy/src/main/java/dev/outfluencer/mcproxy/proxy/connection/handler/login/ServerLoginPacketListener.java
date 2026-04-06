package dev.outfluencer.mcproxy.proxy.connection.handler.login;

import dev.outfluencer.mcproxy.api.ProxyServer;
import dev.outfluencer.mcproxy.api.events.CompressionChangeEvent;
import dev.outfluencer.mcproxy.api.ServerInfo;
import dev.outfluencer.mcproxy.networking.Util;
import dev.outfluencer.mcproxy.networking.protocol.packets.game.ClientboundBundleDelimiterPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.game.ClientboundStartConfigurationPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.handshake.ServerboundHandshakePacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.login.*;
import dev.outfluencer.mcproxy.networking.protocol.registry.Protocol;
import dev.outfluencer.mcproxy.proxy.MinecraftProxy;
import dev.outfluencer.mcproxy.proxy.config.ProxyConfig;
import dev.outfluencer.mcproxy.proxy.connection.LoginResult;
import dev.outfluencer.mcproxy.proxy.connection.PlayerImpl;
import dev.outfluencer.mcproxy.proxy.connection.ServerImpl;
import dev.outfluencer.mcproxy.proxy.connection.handler.config.ServerConfigurationPacketListener;

import java.net.InetSocketAddress;


public class ServerLoginPacketListener implements ClientboundLoginPacketListener {

    private final MinecraftProxy proxy = MinecraftProxy.getInstance();
    private final ServerImpl server;
    private final PlayerImpl player;

    public ServerLoginPacketListener(ServerImpl server) {
        this.server = server;
        this.player = server.getPlayer();
    }

    @Override
    public void onConnect() {
        ServerboundHandshakePacket original = player.getHandshake();
        ServerboundHandshakePacket handshakeCopy = new ServerboundHandshakePacket(original.getVersion(), original.getHostName(), original.getPort(), original.getClientIntent());
        if(proxy.getConfig().getDataForwarding() == ProxyConfig.DataForwarding.BUNGEECORD && player.getAddress() instanceof InetSocketAddress inetSocketAddress) {
            String newHost = handshakeCopy.getHostName() + "\00" + Util.sanitizeAddress( inetSocketAddress ) + "\00" + player.getLoginResult().getId();
            LoginResult result = player.getLoginResult();
            // intellij tells me that result is never null!?
            if(result != null && result.getProperties() != null && result.getProperties().length > 0) {
                newHost += "\00" + LoginResult.GSON.toJson( result.getProperties() );
            }
            handshakeCopy.setHostName(newHost);
        }
        server.sendPacket(handshakeCopy);
        server.getConnection().setProtocol(Protocol.LOGIN);
        server.sendPacket(new ServerboundHelloPacket(player.getName(), player.getUuid()));
    }

    @Override
    public boolean handle(ClientboundLoginDisconnectPacket packet) {
        server.disconnect();
        return DROP;
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
                return DROP;
            }
        }
        updatePlayerServer(packet);
        return DROP;
    }

    @Override
    public boolean handle(ClientboundLoginCompressionPacket packet) {
        server.getConnection().setCompression(packet.getThreshold());
        proxy.getEventManager().fire(new CompressionChangeEvent(packet.getThreshold(), server));
        return DROP;
    }

    @Override
    public boolean handle(ClientboundLoginEncryptionRequestPacket clientboundLoginEncryptionRequestPacket) {
        throw new UnsupportedOperationException("server is in online mode");
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
