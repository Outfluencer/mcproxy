package dev.outfluencer.mcproxy.networking.protocol.packets.config;

import dev.outfluencer.mcproxy.networking.protocol.packets.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
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
public class ServerboundSelectKnownPacks extends Packet<ServerboundConfigurationPacketListener> {

    private List<KnownPack> knownPacks;

    @Override
    public void read(ByteBuf byteBuf, int version) {
        int amount = readVarInt(byteBuf);
        if (amount < 0 || amount > 64) {
            throw new DecoderException("amount out of range");
        }
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
    public boolean handle(ServerboundConfigurationPacketListener listener) {
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
