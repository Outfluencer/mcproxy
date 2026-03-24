package dev.outfluencer.mcproxy.networking.protocol.packets.login;

import dev.outfluencer.mcproxy.networking.protocol.packets.Packet;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.UUID;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ServerboundHelloPacket extends Packet<ServerboundLoginPacketListener> {

    private String name;
    private UUID uuid;

    @Override
    public void read(ByteBuf byteBuf, int version) {
        name = readString(byteBuf);
        uuid = readUUID(byteBuf);
    }

    @Override
    public void write(ByteBuf byteBuf, int version) {
        writeString(name, byteBuf);
        writeUUID(uuid, byteBuf);
    }

    @Override
    public boolean handle(ServerboundLoginPacketListener listener) {
        return listener.handle(this);
    }
}
