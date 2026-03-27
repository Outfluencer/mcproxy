package dev.outfluencer.mcproxy.proxy.connection;

import dev.outfluencer.mcproxy.api.connection.Player;
import dev.outfluencer.mcproxy.networking.ConnectionHandle;
import dev.outfluencer.mcproxy.networking.protocol.DecodedPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.Packet;
import dev.outfluencer.mcproxy.networking.protocol.packets.common.ClientboundCommonDisconnectPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.login.ClientboundLoginDisconnectPacket;
import dev.outfluencer.mcproxy.networking.protocol.registry.Protocol;
import lombok.Getter;
import lombok.Setter;
import net.lenni0451.mcstructs.text.TextComponent;
import net.lenni0451.mcstructs.text.serializer.TextComponentSerializer;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.UUID;

@Getter
@Setter
public class PlayerImpl implements Player {

    private final ConnectionHandle connection;

    private String name;
    private UUID uuid;
    private ServerImpl server;

    public PlayerImpl(ConnectionHandle connectionHandle, String name, UUID uuid) {
        this.connection = connectionHandle;
        this.name = name;
        this.uuid = uuid;
    }

    @Override
    public void disconnect(String message) {
        switch (connection.getEncoderProtocol()) {
            case HANDSHAKE, STATUS -> connection.close(null);
            case LOGIN -> connection.close(new ClientboundLoginDisconnectPacket(TextComponent.of(message)));
            case CONFIG, GAME -> connection.close(new ClientboundCommonDisconnectPacket(TextComponent.of(message)));
        }
    }

    @Override
    public SocketAddress getAddress() {
        return connection.getAddress();
    }

    public void connect(InetSocketAddress address) {
        new BackendConnector(this, address).connect();
    }

    public void fallback() {
        connect(new InetSocketAddress("127.0.0.1", 25565));
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
}
