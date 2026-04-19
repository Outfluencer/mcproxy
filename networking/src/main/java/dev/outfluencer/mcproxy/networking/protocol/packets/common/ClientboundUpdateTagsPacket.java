package dev.outfluencer.mcproxy.networking.protocol.packets.common;

import dev.outfluencer.mcproxy.networking.protocol.packets.Packet;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ClientboundUpdateTagsPacket extends Packet<ClientboundCommonPacketListener> {

    private Map<String, Map<String, int[]>> tags;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientboundUpdateTagsPacket other)) return false;
        if (tags == null) return other.tags == null;
        if (other.tags == null) return false;
        if (tags.size() != other.tags.size()) return false;
        for (Map.Entry<String, Map<String, int[]>> registry : tags.entrySet()) {
            Map<String, int[]> otherRegistry = other.tags.get(registry.getKey());
            if (otherRegistry == null) return false;
            Map<String, int[]> thisRegistry = registry.getValue();
            if (thisRegistry.size() != otherRegistry.size()) return false;
            for (Map.Entry<String, int[]> tag : thisRegistry.entrySet()) {
                int[] otherIds = otherRegistry.get(tag.getKey());
                if (otherIds == null && !otherRegistry.containsKey(tag.getKey())) return false;
                if (!Arrays.equals(tag.getValue(), otherIds)) return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        if (tags == null) return 0;
        int h = 0;
        for (Map.Entry<String, Map<String, int[]>> registry : tags.entrySet()) {
            int innerHash = 0;
            for (Map.Entry<String, int[]> tag : registry.getValue().entrySet()) {
                innerHash += tag.getKey().hashCode() ^ Arrays.hashCode(tag.getValue());
            }
            h += registry.getKey().hashCode() ^ innerHash;
        }
        return h;
    }

    @Override
    public void read(ByteBuf byteBuf, int version) {
        int amount = readVarInt(byteBuf);
        Map<String, Map<String, int[]>> tags = new LinkedHashMap<>(amount);

        for (int i = 0; i < amount; i++) {
            String key = readString(byteBuf);
            int innerAmount = readVarInt(byteBuf);
            Map<String, int[]> innerTags = new LinkedHashMap<>(innerAmount);
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
