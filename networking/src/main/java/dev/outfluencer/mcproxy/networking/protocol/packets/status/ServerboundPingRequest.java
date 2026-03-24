package dev.outfluencer.mcproxy.networking.protocol.packets.status;

import dev.outfluencer.mcproxy.networking.protocol.packets.Packet;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public final class ServerboundPingRequest extends Packet<ServerboundStatusPacketListener> {

    private long ping;

    @Override
    public void read(ByteBuf byteBuf, int version) {
        ping = byteBuf.readLong();
    }

    @Override
    public void write(ByteBuf byteBuf, int version) {
        byteBuf.writeLong(ping);
    }

    @Override
    public boolean handle(ServerboundStatusPacketListener listener) {
        return listener.handle(this);
    }
}
