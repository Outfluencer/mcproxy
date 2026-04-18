package dev.outfluencer.mcproxy.networking.protocol.packets.login;

import dev.outfluencer.mcproxy.networking.protocol.packets.Packet;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ClientboundLoginCompressionPacket extends Packet<ClientboundLoginPacketListener> {

    private int threshold;

    @Override
    public void read(ByteBuf byteBuf, int version) {
        threshold = readVarInt(byteBuf);
    }

    @Override
    public void write(ByteBuf byteBuf, int version) {
        writeVarInt(threshold, byteBuf);
    }

    @Override
    public boolean handle(ClientboundLoginPacketListener listener) {
        return listener.handle(this);
    }
}
