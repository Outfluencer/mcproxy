package dev.outfluencer.mcproxy.networking.protocol.packets.status;

import dev.outfluencer.mcproxy.networking.protocol.packets.Packet;
import io.netty.buffer.ByteBuf;
import lombok.ToString;

@ToString
public final class ServerboundStatusRequestPacket extends Packet<ServerboundStatusPacketListener> {

    public static ServerboundStatusRequestPacket INSTANCE = new ServerboundStatusRequestPacket();

    private ServerboundStatusRequestPacket() {
    }

    @Override
    public void read(ByteBuf byteBuf, int version) {

    }

    @Override
    public void write(ByteBuf byteBuf, int version) {

    }

    @Override
    public boolean handle(ServerboundStatusPacketListener listener) {
        return listener.handle(this);
    }
}
