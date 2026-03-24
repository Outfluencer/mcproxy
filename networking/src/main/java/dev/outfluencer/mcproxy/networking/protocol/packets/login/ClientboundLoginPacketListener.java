package dev.outfluencer.mcproxy.networking.protocol.packets.login;

import dev.outfluencer.mcproxy.networking.protocol.PacketListener;

public interface ClientboundLoginPacketListener extends PacketListener {
    boolean handle(ClientboundLoginDisconnectPacket packet);

    boolean handle(ClientboundLoginFinishedPacket packet);
}
