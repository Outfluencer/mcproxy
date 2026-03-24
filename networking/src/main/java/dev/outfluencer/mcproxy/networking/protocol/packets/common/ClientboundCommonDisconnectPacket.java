package dev.outfluencer.mcproxy.networking.protocol.packets.common;

import dev.outfluencer.mcproxy.networking.protocol.packets.Packet;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import net.lenni0451.mcstructs.text.TextComponent;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ClientboundCommonDisconnectPacket extends Packet<ClientboundCommonPacketListener> {

    private TextComponent reason;

    @Override
    public void read(ByteBuf byteBuf, int version) {
        reason = readComponent(byteBuf, version);
    }

    @Override
    public void write(ByteBuf byteBuf, int version) {
        writeBaseComponent(reason, byteBuf, version);
    }

    @Override
    public boolean handle(ClientboundCommonPacketListener listener) {
        return listener.handle(this);
    }
}
