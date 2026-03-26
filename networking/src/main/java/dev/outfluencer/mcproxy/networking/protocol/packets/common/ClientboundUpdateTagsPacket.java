package dev.outfluencer.mcproxy.networking.protocol.packets.common;

import dev.outfluencer.mcproxy.networking.protocol.packets.Packet;
import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;

public class ClientboundUpdateTagsPacket extends Packet<ClientboundCommonPacketListener> {

    private Map<String, Map<String, int[]>> tags;

    @Override
    public void read(ByteBuf byteBuf, int version) {
        int amount = readVarInt(byteBuf);
        Map<String, Map<String, int[]>> tags = new HashMap<>(amount);

        for (int i = 0; i < amount; i++) {
            String key = readString(byteBuf);
            int innerAmount = readVarInt(byteBuf);
            Map<String, int[]> innerTags = new HashMap<>(innerAmount);
            for (int j = 0; j < innerAmount; j++) {
                innerTags.put(readString(byteBuf), readVarIntArray(byteBuf));
            }
            tags.put(key, innerTags);
        }

        this.tags = tags;
    }

    @Override
    public void write(ByteBuf byteBuf, int version) {
        writeVarInt(tags.size(), byteBuf);
        for (Map.Entry<String, Map<String, int[]>> entry : tags.entrySet()) {
            writeString(entry.getKey(), byteBuf);
            writeVarInt(entry.getValue().size(), byteBuf);
            for (Map.Entry<String, int[]> innerEntry : entry.getValue().entrySet()) {
                writeString(innerEntry.getKey(), byteBuf);
                writeVarIntArray(innerEntry.getValue(), byteBuf);
            }
        }
    }

    @Override
    public boolean handle(ClientboundCommonPacketListener listener) {
        return listener.handle(this);
    }
}
