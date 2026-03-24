package dev.outfluencer.mcproxy.networking.protocol.packets.status;

import dev.outfluencer.mcproxy.networking.protocol.PacketListener;

public interface ClientboundStatusPacketListener extends PacketListener {

    boolean handle(ClientboundStatusResponsePacket packet);
    boolean handle(ClientboundPongResponsePacket packet);

}
