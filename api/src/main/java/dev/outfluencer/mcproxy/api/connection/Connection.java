package dev.outfluencer.mcproxy.api.connection;

import dev.outfluencer.mcproxy.networking.protocol.packets.Packet;
import io.netty.channel.Channel;
import net.lenni0451.mcstructs.text.TextComponent;

import java.net.SocketAddress;

public interface Connection {

    String getName();

    void disconnect(String message);
    void disconnect(TextComponent message);

    SocketAddress getAddress();

    interface Unsafe {
        Channel getHandle();
        void sendPacket(Packet<?> packet);
    }

    Unsafe getUnsafe();
}
