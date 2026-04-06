package dev.outfluencer.mcproxy.networking.protocol.packets.game;

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
@NoArgsConstructor
@AllArgsConstructor
public class ClientboundCommandSuggestionsPacket extends Packet<ClientboundGamePacketListener> {

    private int transactionId;
    private int rangeStart;
    private int rangeLength;
    private Entry[] entries;

    @Override
    public void read(ByteBuf buf, int version) {
        transactionId = readVarInt(buf);
        rangeStart = readVarInt(buf);
        rangeLength = readVarInt(buf);
        int count = readVarInt(buf);
        entries = new Entry[count];
        for (int i = 0; i < count; i++) {
            String text = readString(buf);
            TextComponent tooltip = buf.readBoolean() ? readComponent(buf, version) : null;
            entries[i] = new Entry(text, tooltip);
        }
    }

    @Override
    public void write(ByteBuf buf, int version) {
        writeVarInt(transactionId, buf);
        writeVarInt(rangeStart, buf);
        writeVarInt(rangeLength, buf);
        writeVarInt(entries.length, buf);
        for (Entry entry : entries) {
            writeString(entry.text, buf);
            buf.writeBoolean(entry.tooltip != null);
            if (entry.tooltip != null) {
                writeBaseComponent(entry.tooltip, buf, version);
            }
        }
    }

    @Override
    public boolean handle(ClientboundGamePacketListener listener) {
        return listener.handle(this);
    }

    public record Entry(String text, TextComponent tooltip) {
    }
}
