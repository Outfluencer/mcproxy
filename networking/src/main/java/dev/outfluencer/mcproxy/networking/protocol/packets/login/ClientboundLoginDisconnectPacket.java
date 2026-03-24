package dev.outfluencer.mcproxy.networking.protocol.packets.login;

import dev.outfluencer.mcproxy.networking.Util;
import dev.outfluencer.mcproxy.networking.protocol.packets.Packet;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.lenni0451.mcstructs.text.TextComponent;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ClientboundLoginDisconnectPacket extends Packet<ClientboundLoginPacketListener> {

    private TextComponent reason;

    @Override
    public void read(ByteBuf byteBuf, int version) {
        reason = Util.textComponentSerializerByVersion(version).deserialize(readString(byteBuf));
    }

    @Override
    public void write(ByteBuf byteBuf, int version) {
        writeString(Util.textComponentSerializerByVersion(version).serialize(reason), byteBuf);
    }

    @Override
    public boolean handle(ClientboundLoginPacketListener listener) {
        return listener.handle(this);
    }
}
