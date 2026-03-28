package dev.outfluencer.mcproxy.proxy.connection.handler.login;

import dev.outfluencer.mcproxy.config.ServerInfo;
import dev.outfluencer.mcproxy.networking.ConnectionHandle;
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
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ServerLoginPacketListener implements ClientboundLoginPacketListener {

    private final ConnectionHandle backendHandle;
    private final PlayerImpl player;
    private final ServerInfo serverInfo;

    @Override
    public void onConnect() {
        ServerboundHandshakePacket handshake = new ServerboundHandshakePacket(player.getConnection().getProtocolVersion(), serverInfo.getAddress(), serverInfo.getPort(), ServerboundHandshakePacket.ClientIntent.LOGIN);
        backendHandle.sendPacket(handshake);
        backendHandle.setProtocol(Protocol.LOGIN);
        backendHandle.sendPacket(new ServerboundHelloPacket(player.getName(), player.getUuid()));
    }

    @Override
    public boolean handle(ClientboundLoginDisconnectPacket packet) {
        backendHandle.close(null);
        return false;
    }

    @Override
    public boolean handle(ClientboundLoginFinishedPacket packet) {
        ServerImpl server = player.getServer();
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
        backendHandle.setCompression(packet.getThreshold());
        return false;
    }

    public void updatePlayerServer(ClientboundLoginFinishedPacket packet) {
        if (!player.isConnected()) {
            return;
        }

        ServerImpl server = new ServerImpl(serverInfo, player, backendHandle);
        backendHandle.setPacketListener(new ServerConfigurationPacketListener(server));
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
            backendHandle.sendPacket(new ServerboundLoginAcknowledgedPacket());
        }

    }

    @Override
    public String toString() {
        String name = player != null ? player.getName() : null;
        return "[" + getClass().getSimpleName() + "|" + (name != null ? name + "|" : "") + backendHandle.getAddress() + "]";
    }

    @Override
    public void onDisconnect() {
        if(player.getFallbackConnects() != null) {
            player.connectToNextFallback();
        }
    }
}
