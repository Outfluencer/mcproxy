package dev.outfluencer.mcproxy.proxy.connection.handler.common;

import dev.outfluencer.mcproxy.networking.protocol.packets.common.ClientboundCommonDisconnectPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.common.ClientboundCommonPacketListener;

public class ServerboundCommonPacketListenerImpl implements ClientboundCommonPacketListener {
    @Override
    public boolean handle(ClientboundCommonDisconnectPacket packet) {
        return false;
    }
}
