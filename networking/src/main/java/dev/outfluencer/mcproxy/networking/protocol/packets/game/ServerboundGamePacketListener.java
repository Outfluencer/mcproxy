package dev.outfluencer.mcproxy.networking.protocol.packets.game;

import dev.outfluencer.mcproxy.networking.protocol.PacketListener;

public interface ServerboundGamePacketListener extends PacketListener {
    boolean handle(ServerboundConfigurationAcknowledgedPacket packet);

    boolean handle(ServerboundChatCommandPacket serverboundChatCommandPacket);
}
