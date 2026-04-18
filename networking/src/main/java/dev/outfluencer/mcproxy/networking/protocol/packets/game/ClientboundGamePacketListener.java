package dev.outfluencer.mcproxy.networking.protocol.packets.game;

import dev.outfluencer.mcproxy.networking.protocol.PacketListener;

public interface ClientboundGamePacketListener extends PacketListener {
    boolean handle(ClientboundStartConfigurationPacket packet);

    boolean handle(ClientboundBundleDelimiterPacket clientboundBundleDelimiterPacket);

    boolean handle(ClientboundSystemChatPacket clientboundSystemChatPacket);

    boolean handle(ClientboundCommandsPacket clientboundCommandsPacket);

    boolean handle(ClientboundCommandSuggestionsPacket clientboundCommandSuggestionsPacket);

    boolean handle(ClientboundRespawnPacket clientboundRespawnDelimiterPacket);
}
