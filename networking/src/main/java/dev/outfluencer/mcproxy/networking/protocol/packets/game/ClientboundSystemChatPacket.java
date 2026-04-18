package dev.outfluencer.mcproxy.networking.protocol.packets.game;

import dev.outfluencer.mcproxy.networking.protocol.packets.Packet;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import net.lenni0451.mcstructs.text.TextComponent;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ClientboundSystemChatPacket extends Packet<ClientboundGamePacketListener> {

    private TextComponent message;
    private boolean overlay;

    @Override
    public void read(ByteBuf byteBuf, int version) {
        message = readComponent(byteBuf, version);
        overlay = byteBuf.readBoolean();
    }

    @Override
    public void write(ByteBuf byteBuf, int version) {
        writeBaseComponent(message, byteBuf, version);
        byteBuf.writeBoolean(overlay);
    }

    @Override
    public boolean handle(ClientboundGamePacketListener listener) {
        return listener.handle(this);
    }

}
