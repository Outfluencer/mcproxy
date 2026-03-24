package dev.outfluencer.mcproxy.networking.protocol.packets.login;

import dev.outfluencer.mcproxy.networking.Property;
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
public class ClientboundLoginFinishedPacket extends Packet<ClientboundLoginPacketListener> {

    private UUID uuid;
    private String username;
    private Property[] properties;

    @Override
    public void read(ByteBuf byteBuf, int version) {
        uuid = readUUID(byteBuf);
        username = readString(byteBuf);
        properties = readProperties(byteBuf);
    }

    @Override
    public void write(ByteBuf byteBuf, int version) {
        writeUUID(uuid, byteBuf);
        writeString(username, byteBuf);
        writeProperties(properties, byteBuf);
    }

    @Override
    public boolean handle(ClientboundLoginPacketListener listener) {
        return listener.handle(this);
    }
}
