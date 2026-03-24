package dev.outfluencer.mcproxy.networking.protocol.packets.game;

import dev.outfluencer.mcproxy.networking.protocol.packets.Packet;
import io.netty.buffer.ByteBuf;

public class ClientboundBundleDelimiterPacket extends Packet<ClientboundGamePacketListener> {
    @Override
    public void read(ByteBuf byteBuf, int version) {

    }

    @Override
    public void write(ByteBuf byteBuf, int version) {

    }

    @Override
    public boolean handle(ClientboundGamePacketListener listener) {
        return listener.handle(this);
    }
}
