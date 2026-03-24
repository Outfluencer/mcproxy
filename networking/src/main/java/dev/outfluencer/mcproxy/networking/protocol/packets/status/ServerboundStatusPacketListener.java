package dev.outfluencer.mcproxy.networking.protocol.packets.status;

import dev.outfluencer.mcproxy.networking.protocol.PacketListener;

public interface ServerboundStatusPacketListener extends PacketListener {

    boolean handle(ServerboundStatusRequestPacket packet);
    boolean handle(ServerboundPingRequest packet);

}
