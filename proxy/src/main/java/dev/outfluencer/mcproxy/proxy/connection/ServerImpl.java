package dev.outfluencer.mcproxy.proxy.connection;

import com.google.common.base.Preconditions;
import dev.outfluencer.mcproxy.api.ServerInfo;
import dev.outfluencer.mcproxy.api.connection.Server;
import dev.outfluencer.mcproxy.networking.ConnectionHandle;
import dev.outfluencer.mcproxy.networking.protocol.DecodedPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.Packet;
import dev.outfluencer.mcproxy.networking.protocol.registry.Protocol;
import io.netty.channel.Channel;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
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
    private boolean discarded;
    private final ConfigurationTracker configurationTracker = new ConfigurationTracker();

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
        return serverInfo.getName();
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

    @Data
    public static class ConfigurationTracker {
        @NonNull
        private final IntHolder pendingKnownPacks = new IntHolder();
        private boolean pendingLoginAck = false;
        private boolean pendingStartConfigAck = false;

        public void assertSafe() {
            Preconditions.checkState(pendingKnownPacks.get() == 0, "pendingKnownPacks.get() == 0");
            Preconditions.checkState(!pendingLoginAck, "!pendingLoginAck");
            Preconditions.checkState(!pendingStartConfigAck, "!pendingStartConfigAck");
        }
    }

    public static class IntHolder {
        private int value;

        public int get() {
            return value;
        }

        public int increment() {
            return ++value;
        }

        public int set(int value) {
            this.value = value;
            return value;
        }

        public int decrement() {
            return --value;
        }

        public int add(int n) {
            value += n;
            return value;
        }

        public int subtract(int n) {
            value -= n;
            return value;
        }
    }
}
