package dev.outfluencer.mcproxy.networking.protocol;

import dev.outfluencer.mcproxy.networking.netty.QuietException;
import dev.outfluencer.mcproxy.networking.protocol.registry.Protocol;

public interface PacketListener {

    boolean DROP = false;
    boolean PASS = true;

    default void onException(Throwable throwable) {

    }

    default void onDisconnect() {

    }

    default void onConnect() {

    }

    default void onWritabilityChanged() {

    }

    default void encoderProtocolChanged(Protocol protocol) {

    }
    default void decoderProtocolChanged(Protocol protocol) {

    }

    default void handle(DecodedPacket decodedPacket) {
        throw new QuietException("Unexpected DecodedPacket");
    }
}
