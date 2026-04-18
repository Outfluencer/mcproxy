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
public class ServerboundCommandSuggestionPacket extends Packet<ServerboundGamePacketListener> {

    private int id;
    private String command;

    @Override
    public void read(ByteBuf byteBuf, int version) {
        id = readVarInt(byteBuf);
        command = readString(byteBuf, 32500);
    }

    @Override
    public void write(ByteBuf byteBuf, int version) {
        writeVarInt(id, byteBuf);
        writeString(command, byteBuf);
    }

    @Override
    public boolean handle(ServerboundGamePacketListener listener) {
        return listener.handle(this);
    }
}
