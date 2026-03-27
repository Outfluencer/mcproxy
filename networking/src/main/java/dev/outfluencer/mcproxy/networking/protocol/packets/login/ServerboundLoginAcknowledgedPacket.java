package dev.outfluencer.mcproxy.networking.protocol.packets.login;

import dev.outfluencer.mcproxy.networking.protocol.packets.Packet;
import dev.outfluencer.mcproxy.networking.protocol.registry.Protocol;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
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

    @Override
    public Protocol nextProtocol() {
        return Protocol.CONFIG;
    }
}
