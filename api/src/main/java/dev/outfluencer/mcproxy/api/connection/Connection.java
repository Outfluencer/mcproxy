package dev.outfluencer.mcproxy.api.connection;

import dev.outfluencer.mcproxy.networking.protocol.packets.Packet;
import io.netty.channel.Channel;

import java.net.SocketAddress;

public interface Connection {

    String getName();

    void disconnect(String message);

    SocketAddress getAddress();

    interface Unsafe {
        Channel getHandle();
        void sendPacket(Packet<?> packet);
    }

    Unsafe getUnsafe();
}
