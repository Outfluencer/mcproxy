package dev.outfluencer.mcproxy.networking.protocol.packets.config;

import dev.outfluencer.mcproxy.networking.protocol.PacketListener;

public interface ServerboundConfigurationPacketListener extends PacketListener {
    boolean handle(ServerboundFinishConfigurationPacket serverboundFinishConfigurationPacket);

    boolean handle(ServerboundSelectKnownPacks serverboundSelectKnownPacks);
}
