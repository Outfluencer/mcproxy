package dev.outfluencer.mcproxy.networking.protocol.packets.handshake;

import dev.outfluencer.mcproxy.networking.protocol.PacketListener;

public interface ServerboundHandshakePacketListener extends PacketListener {

    boolean handle(ServerboundHandshakePacket packet);

}
