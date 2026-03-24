package dev.outfluencer.mcproxy.networking.protocol.packets.status;

import dev.outfluencer.mcproxy.networking.protocol.packets.Packet;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ClientboundPongResponsePacket extends Packet<ClientboundStatusPacketListener> {

    private long time;

    @Override
    public void read(ByteBuf byteBuf, int version) {
        time = byteBuf.readLong();
    }

    @Override
    public void write(ByteBuf byteBuf, int version) {
        byteBuf.writeLong(time);
    }

    @Override
    public boolean handle(ClientboundStatusPacketListener listener) {
        return listener.handle(this);
    }
}
