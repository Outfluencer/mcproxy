package dev.outfluencer.mcproxy.networking.protocol.packets.common;

import dev.outfluencer.mcproxy.networking.protocol.PacketListener;

public interface ClientboundCommonPacketListener extends PacketListener {

    boolean handle(ClientboundCommonDisconnectPacket packet);
}
