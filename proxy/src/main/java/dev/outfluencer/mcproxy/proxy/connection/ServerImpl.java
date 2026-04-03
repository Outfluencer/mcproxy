package dev.outfluencer.mcproxy.proxy.connection;

import dev.outfluencer.mcproxy.api.connection.Server;
import dev.outfluencer.mcproxy.api.ServerInfo;
import dev.outfluencer.mcproxy.networking.ConnectionHandle;
import dev.outfluencer.mcproxy.networking.protocol.DecodedPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.Packet;
import dev.outfluencer.mcproxy.networking.protocol.registry.Protocol;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.lenni0451.mcstructs.text.TextComponent;

import java.net.SocketAddress;

@Getter
@RequiredArgsConstructor
public class ServerImpl implements Server {
    private final ServerInfo serverInfo;
    private final PlayerImpl player;
    private final ConnectionHandle connection;
    @Setter
    private ConfigurationTracker configurationTracker;
    public boolean isConnected() {
        return !connection.isClosed();
    }

    public void disconnect() {
        connection.close(null);
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

    @Override
    public String getName() {
        return String.valueOf(getAddress());
    }

    @Override
    public void disconnect(String message) {
        disconnect();
    }

    @Override
    public void disconnect(TextComponent message) {
        disconnect();
    }

    @Override
    public SocketAddress getAddress() {
        return connection.getAddress();
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

    public static class ConfigurationTracker {
        public int pendingKnownPacks;
        public boolean pendingLoginAck;
        public boolean pendingStartConfigAck;
    }
}
