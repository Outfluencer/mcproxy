package dev.outfluencer.mcproxy.networking.protocol.packets.config;

import dev.outfluencer.mcproxy.networking.protocol.PacketListener;

public interface ClientboundConfigurationPacketListener extends PacketListener {
    boolean handle(ClientboundFinishConfigurationPacket packet);
}
