package dev.outfluencer.mcproxy.networking.protocol.packets.config;

import dev.outfluencer.mcproxy.networking.protocol.packets.Packet;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.lenni0451.mcstructs.nbt.NbtTag;

import java.util.ArrayList;
import java.util.List;

public class ClientboundRegistryDataPacket extends Packet<ClientboundConfigurationPacketListener> {

    private String registryId;
    private List<RegistryEntry> entries;

    @Override
    public void read(ByteBuf byteBuf, int version) {
        registryId = readString(byteBuf);
        int amount = readVarInt(byteBuf);
        List<RegistryEntry> entries = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            String string = readString(byteBuf);
            NbtTag tag = null;
            if (byteBuf.readBoolean()) {
                tag = readTag(byteBuf, version);
            }
            entries.add(new RegistryEntry(string, tag));
        }
        this.entries = entries;
    }

    @Override
    public void write(ByteBuf byteBuf, int version) {
        writeString(registryId, byteBuf);
        writeVarInt(entries.size(), byteBuf);
        for (RegistryEntry entry : entries) {
            writeString(entry.entryId, byteBuf);
            if (entry.tag != null) {
                byteBuf.writeBoolean(true);
                writeTag(entry.tag, byteBuf, version);
            } else {
                byteBuf.writeBoolean(false);
            }
        }
    }

    @Override
    public boolean handle(ClientboundConfigurationPacketListener listener) {
        return listener.handle(this);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegistryEntry {
        private String entryId;
        private NbtTag tag;
    }
}
