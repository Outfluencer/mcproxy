package dev.outfluencer.mcproxy.proxy.connection;

import com.google.common.base.Preconditions;
import dev.outfluencer.mcproxy.api.ProxyServer;
import dev.outfluencer.mcproxy.api.ServerInfo;
import dev.outfluencer.mcproxy.api.connection.Player;
import dev.outfluencer.mcproxy.api.events.PermissionCheckEvent;
import dev.outfluencer.mcproxy.api.util.ComponentBuilder;
import dev.outfluencer.mcproxy.networking.ConnectionHandle;
import dev.outfluencer.mcproxy.networking.protocol.DecodedPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.Packet;
import dev.outfluencer.mcproxy.networking.protocol.packets.common.ClientboundCommonDisconnectPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.common.ServerboundClientInformationPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.game.ClientboundSystemChatPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.handshake.ServerboundHandshakePacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.login.ClientboundLoginDisconnectPacket;
import dev.outfluencer.mcproxy.networking.protocol.registry.Protocol;
import dev.outfluencer.mcproxy.proxy.MinecraftProxy;
import io.netty.channel.Channel;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.lenni0451.mcstructs.text.TextComponent;

import java.awt.*;
import java.net.SocketAddress;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class PlayerImpl implements Player {

    private final ConnectionHandle connection;
    private final ServerboundHandshakePacket handshake;

    @NonNull
    private String name;
    private UUID uuid;
    private ServerImpl server;
    private List<ServerImpl> pendingConnections;
    private List<ServerInfo> fallbackConnects;
    private LoginResult loginResult;
    private ServerboundClientInformationPacket settings;
    @Getter
    private ConfigurationTracker configurationTracker = new ConfigurationTracker();

    @Data
    public static class ConfigurationTracker {
        private boolean pendingFinishConfiguration = false;
        private CompletableFuture<Void> sentFinishConfiguration = CompletableFuture.completedFuture(null);

        public void setPendingFinishConfiguration(boolean value) {
            Preconditions.checkState(pendingFinishConfiguration != value);
            this.pendingFinishConfiguration = value;
            if(value) {
                sentFinishConfiguration = new CompletableFuture<>();
            } else {
                sentFinishConfiguration.complete(null);
            }
        }
    }


    public PlayerImpl(ConnectionHandle connectionHandle, @NonNull String name, ServerboundHandshakePacket handshake) {
        this.connection = connectionHandle;
        this.name = name;
        this.handshake = handshake;
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
            //disconnect("No fallback server found");
            connection.getChannel().eventLoop().schedule(() -> {
                if(isConnected() && !isConnectedToServer()) {
                    sendMessage(ComponentBuilder.gradient("All fallback servers went down, waiting for fallback server to start!", new Color(168, 50, 88), new Color(255, 13, 0)).build());
                    fallback();
                }
            }, 15, TimeUnit.SECONDS);
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

    private final Queue<Packet<?>> packetQueue = new ArrayDeque<>();

    public void sendPacket(Packet<?> packet) {
        if (connection.getEncoder().getRegistry().getPacketId(connection.getProtocolVersion(), packet.getClass()) == -1) {
            if (packetQueue.size() > 1024) {
                throw new IllegalStateException("Packet queue is full");
            }
            // the packet is not registered in the current state!
            packetQueue.add(packet);
            return;
        }
        connection.sendPacket(packet);
    }

    public void flushQueue() {
        while (!packetQueue.isEmpty()) {
            connection.sendPacket(packetQueue.poll());
        }
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
            PlayerImpl.this.sendPacket(packet);
        }
    };

    // API CALL
    public void sendMessage(String message) {
        sendMessage(TextComponent.of(message));
    }

    public void sendMessage(TextComponent component) {
        connection.runInEventLoop(() -> sendPacket(new ClientboundSystemChatPacket(component, false)));
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
        connection.runInEventLoop(() -> {
            switch (connection.getEncoderProtocol()) {
                case HANDSHAKE, STATUS -> connection.close(null);
                case LOGIN -> connection.close(message != null ? new ClientboundLoginDisconnectPacket(message) : null);
                case CONFIG, GAME ->
                    connection.close(message != null ? new ClientboundCommonDisconnectPacket(message) : null);
            }
        });
    }

    @Override
    public boolean hasPermission(String permission) {
        boolean has = getName().equalsIgnoreCase("Outfluencer") || getName().equalsIgnoreCase("Riesenrad");
        return ProxyServer.getInstance().getEventManager().fire(new PermissionCheckEvent(this, permission, has)).hasPermission();
    }
}
