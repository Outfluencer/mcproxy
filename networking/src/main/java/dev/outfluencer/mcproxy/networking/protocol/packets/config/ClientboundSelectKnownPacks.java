package dev.outfluencer.mcproxy.networking.protocol.packets.config;

import dev.outfluencer.mcproxy.networking.protocol.packets.Packet;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class ClientboundSelectKnownPacks extends Packet<ClientboundConfigurationPacketListener> {

    private List<KnownPack> knownPacks;

    @Override
    public void read(ByteBuf byteBuf, int version) {
        int amount = readVarInt(byteBuf);
        List<KnownPack> packs = new ArrayList<>(amount);
        for (int i = 0; i < amount; i++) {
            packs.add(new KnownPack(readString(byteBuf), readString(byteBuf), readString(byteBuf)));
        }
        knownPacks = packs;
    }

    @Override
    public void write(ByteBuf byteBuf, int version) {
        writeVarInt(knownPacks.size(), byteBuf);
        for (KnownPack pack : knownPacks) {
            writeString(pack.getNamespace(), byteBuf);
            writeString(pack.getId(), byteBuf);
            writeString(pack.getVersion(), byteBuf);
        }
    }

    @Override
    public boolean handle(ClientboundConfigurationPacketListener listener) {
        return listener.handle(this);
    }

    @Data
    @AllArgsConstructor
    public static class KnownPack {
        private String namespace;
        private String id;
        private String version;
    }
}
