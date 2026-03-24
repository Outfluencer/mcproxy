package dev.outfluencer.mcproxy.networking.protocol;

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

    }
}
