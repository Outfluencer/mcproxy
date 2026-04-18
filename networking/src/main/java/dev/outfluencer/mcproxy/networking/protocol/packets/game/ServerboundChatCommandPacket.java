package dev.outfluencer.mcproxy.networking.protocol.packets.game;

import dev.outfluencer.mcproxy.networking.protocol.packets.Packet;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ServerboundChatCommandPacket extends Packet<ServerboundGamePacketListener> {

    private String message;

    @Override
    public void read(ByteBuf byteBuf, int version) {
        message = readString(byteBuf);
    }

    @Override
    public void write(ByteBuf byteBuf, int version) {
        writeString(message, byteBuf);
    }

    @Override
    public boolean handle(ServerboundGamePacketListener listener) {
        return listener.handle(this);
    }
}
