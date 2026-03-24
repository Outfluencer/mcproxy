package dev.outfluencer.mcproxy.networking.protocol.packets.config;

import dev.outfluencer.mcproxy.networking.protocol.packets.Packet;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class ServerboundFinishConfigurationPacket extends Packet<ServerboundConfigurationPacketListener> {
    @Override
    public void read(ByteBuf byteBuf, int version) {

    }

    @Override
    public void write(ByteBuf byteBuf, int version) {

    }

    @Override
    public boolean handle(ServerboundConfigurationPacketListener listener) {
        return listener.handle(this);
    }
}
