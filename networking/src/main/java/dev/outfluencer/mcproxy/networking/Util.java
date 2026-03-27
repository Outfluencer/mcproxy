package dev.outfluencer.mcproxy.networking;

import dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion;
import net.lenni0451.mcstructs.text.serializer.TextComponentCodec;
import net.lenni0451.mcstructs.text.serializer.TextComponentSerializer;

public class Util {

    public static TextComponentSerializer textComponentSerializerByVersion(int protocolVersion) {
        return switch (protocolVersion) {
            case MinecraftVersion.V1_21_11, MinecraftVersion.V26_1 -> TextComponentSerializer.V1_21_9;
            default -> throw new IllegalStateException("Unexpected value: " + protocolVersion);
        };
    }

    public static TextComponentCodec textComponentCodecByVersion(int protocolVersion) {
        return switch (protocolVersion) {
            case MinecraftVersion.V1_21_11, MinecraftVersion.V26_1 -> TextComponentCodec.V1_21_9;
            default -> throw new IllegalStateException("Unexpected value: " + protocolVersion);
        };
    }

}
