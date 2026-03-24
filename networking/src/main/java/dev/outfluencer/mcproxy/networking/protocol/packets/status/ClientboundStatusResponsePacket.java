package dev.outfluencer.mcproxy.networking.protocol.packets.status;

import dev.outfluencer.mcproxy.networking.ServerStatus;
import dev.outfluencer.mcproxy.networking.protocol.packets.Packet;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ClientboundStatusResponsePacket extends Packet<ClientboundStatusPacketListener> {

    private ServerStatus response;

    @Override
    public void read(ByteBuf byteBuf, int version) {
        response = ServerStatus.deserialize(readString(byteBuf), version);
    }

    @Override
    public void write(ByteBuf byteBuf, int version) {
        writeString(response.serialize(version), byteBuf);
    }

    @Override
    public boolean handle(ClientboundStatusPacketListener listener) {
        return listener.handle(this);
    }
}
