package dev.outfluencer.mcproxy.networking.protocol.packets.login;

import dev.outfluencer.mcproxy.networking.protocol.packets.Packet;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
public class ServerboundLoginAcknowledgedPacket extends Packet<ServerboundLoginPacketListener> {

    public static final ServerboundLoginAcknowledgedPacket INSTANCE = new ServerboundLoginAcknowledgedPacket();

    @Override
    public void read(ByteBuf byteBuf, int version) {

    }

    @Override
    public void write(ByteBuf byteBuf, int version) {

    }

    @Override
    public boolean handle(ServerboundLoginPacketListener listener) {
        return listener.handle(this);
    }
}
