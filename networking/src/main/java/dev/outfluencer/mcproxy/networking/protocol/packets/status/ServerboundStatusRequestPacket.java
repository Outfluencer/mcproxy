package dev.outfluencer.mcproxy.networking.protocol.packets.status;

import dev.outfluencer.mcproxy.networking.protocol.packets.Packet;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public final class ServerboundStatusRequestPacket extends Packet<ServerboundStatusPacketListener> {

    public static ServerboundStatusRequestPacket INSTANCE = new ServerboundStatusRequestPacket();

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
