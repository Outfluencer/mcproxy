package dev.outfluencer.mcproxy.networking.protocol.packets.login;

import dev.outfluencer.mcproxy.networking.Util;
import dev.outfluencer.mcproxy.networking.protocol.packets.Packet;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.lenni0451.mcstructs.text.TextComponent;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ClientboundLoginCompressionPacket extends Packet<ClientboundLoginPacketListener> {

    private int threshold;

    @Override
    public void read(ByteBuf byteBuf, int version) {
        threshold = readVarInt(byteBuf);
    }

    @Override
    public void write(ByteBuf byteBuf, int version) {
        writeVarInt(threshold, byteBuf);
    }

    @Override
    public boolean handle(ClientboundLoginPacketListener listener) {
        return listener.handle(this);
    }
}
