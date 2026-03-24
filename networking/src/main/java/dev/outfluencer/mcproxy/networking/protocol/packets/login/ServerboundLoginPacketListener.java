package dev.outfluencer.mcproxy.networking.protocol.packets.login;

import dev.outfluencer.mcproxy.networking.protocol.PacketListener;
import dev.outfluencer.mcproxy.networking.protocol.packets.status.ServerboundPingRequest;
import dev.outfluencer.mcproxy.networking.protocol.packets.status.ServerboundStatusRequestPacket;

public interface ServerboundLoginPacketListener extends PacketListener {
    boolean handle(ServerboundHelloPacket packet);

    boolean handle(ServerboundLoginAcknowledgedPacket packet);
}
