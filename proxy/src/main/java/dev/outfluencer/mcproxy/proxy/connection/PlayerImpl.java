package dev.outfluencer.mcproxy.proxy.connection;

import dev.outfluencer.mcproxy.api.ServerInfo;
import dev.outfluencer.mcproxy.api.connection.Player;
import dev.outfluencer.mcproxy.networking.ConnectionHandle;
import dev.outfluencer.mcproxy.networking.protocol.DecodedPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.Packet;
import dev.outfluencer.mcproxy.networking.protocol.packets.common.ClientboundCommonDisconnectPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.handshake.ServerboundHandshakePacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.login.ClientboundLoginDisconnectPacket;
import dev.outfluencer.mcproxy.networking.protocol.registry.Protocol;
import dev.outfluencer.mcproxy.proxy.MinecraftProxy;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.lenni0451.mcstructs.text.TextComponent;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class PlayerImpl implements Player {

    private final ConnectionHandle connection;
    private final ServerboundHandshakePacket handshake;

    @NonNull
    private String name;
    @NonNull
    private UUID uuid;
    private ServerImpl server;
    private List<ServerImpl> pendingConnections;
    private List<ServerInfo> fallbackConnects;
    private LoginResult loginResult;


    public PlayerImpl(ConnectionHandle connectionHandle, String name, UUID uuid, ServerboundHandshakePacket handshake) {
        this.connection = connectionHandle;
        this.name = name;
        this.uuid = uuid;
        this.handshake = handshake;
    }

    @Override
    public void disconnect(String message) {
        if (message == null) {
            disconnect((TextComponent) null);
        } else {
            disconnect(TextComponent.of(message));
        }
    }

    @Override
    public void disconnect(TextComponent message) {
        switch (connection.getEncoderProtocol()) {
            case HANDSHAKE, STATUS -> connection.close(null);
            case LOGIN -> connection.close(message != null ? new ClientboundLoginDisconnectPacket(message) : null);
            case CONFIG, GAME ->
                    connection.close(message != null ? new ClientboundCommonDisconnectPacket(message) : null);
        }
    }

    @Override
    public SocketAddress getAddress() {
        return connection.getAddress();
    }

    public void connect(ServerInfo serverInfo) {
        new BackendConnector(this, serverInfo).connect();
    }

    public void fallback() {
        assert fallbackConnects == null;
        fallbackConnects = MinecraftProxy.getInstance().getConfig().getFallbackServers();
        connectToNextFallback();
    }

    public void connectToNextFallback() {
        if (fallbackConnects == null || fallbackConnects.isEmpty()) {
            disconnect("No fallback server found");
            return;
        }
        connect(fallbackConnects.removeFirst());

        if (fallbackConnects.isEmpty()) {
            fallbackConnects = null;
        }
    }

    public void setServer(ServerImpl server) {
        this.server = server;
        if (pendingConnections != null) {
            pendingConnections.remove(server);
        }
        fallbackConnects = null;
    }

    public void addPendingConnection(ServerImpl server) {
        if (pendingConnections == null) {
            pendingConnections = new ArrayList<>();
        }
        pendingConnections.add(server);
    }

    public void disconnectPendingConnections() {
        List<ServerImpl> pending = this.pendingConnections;
        this.pendingConnections = null;
        if (pending != null) {
            for (ServerImpl server : pending) {
                if (server.isConnected()) {
                    server.disconnect();
                }
            }
        }
    }

    private boolean bundling;

    public void toggleBundle() {
        bundling = !bundling;
    }

    public boolean isConnected() {
        return !connection.isClosed();
    }

    public void sendPacket(Packet<?> packet) {
        connection.sendPacket(packet);
    }

    public void sendDecodedPacket(DecodedPacket decodedPacket) {
        connection.sendDecodedPacket(decodedPacket);
    }

    public Protocol getDecoderProtocol() {
        return connection.getDecoderProtocol();
    }

    public Protocol getEncoderProtocol() {
        return connection.getEncoderProtocol();
    }

    public boolean isConnectedToServer() {
        return server != null && server.isConnected();
    }

    @Getter
    private final Unsafe unsafe = new Unsafe() {
        @Override
        public Channel getHandle() {
            return connection.getChannel();
        }

        @Override
        public void sendPacket(Packet<?> packet) {
            connection.sendPacket(packet);
        }
    };

}
