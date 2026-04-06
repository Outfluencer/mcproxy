package dev.outfluencer.mcproxy.networking.protocol.packets.common;

import dev.outfluencer.mcproxy.networking.protocol.PacketListener;

public interface ServerboundCommonPacketListener extends PacketListener {

    boolean handle(ServerboundClientInformationPacket serverboundClientInformationPacket);
}
