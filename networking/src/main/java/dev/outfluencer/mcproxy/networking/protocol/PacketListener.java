package dev.outfluencer.mcproxy.networking.protocol;

import dev.outfluencer.mcproxy.networking.netty.QuietException;

public interface PacketListener {

    default void onException(Throwable throwable) {

    }

    default void onDisconnect() {

    }

    default void onConnect() {

    }

    default void onWritabilityChanged() {

    }

    default void handle(DecodedPacket decodedPacket) {
        throw new QuietException("Unexpected DecodedPacket");
    }
}
