package dev.outfluencer.mcproxy.networking;

import net.lenni0451.mcstructs.text.serializer.TextComponentCodec;
import net.lenni0451.mcstructs.text.serializer.TextComponentSerializer;

import java.net.Inet6Address;
import java.net.InetSocketAddress;

import static dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion.V1_10;
import static dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion.V1_11;
import static dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion.V1_11_1;
import static dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion.V1_12;
import static dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion.V1_12_1;
import static dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion.V1_12_2;
import static dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion.V1_13;
import static dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion.V1_13_1;
import static dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion.V1_13_2;
import static dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion.V1_14;
import static dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion.V1_14_1;
import static dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion.V1_14_2;
import static dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion.V1_14_3;
import static dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion.V1_14_4;
import static dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion.V1_15;
import static dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion.V1_15_1;
import static dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion.V1_15_2;
import static dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion.V1_16;
import static dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion.V1_16_1;
import static dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion.V1_16_2;
import static dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion.V1_16_3;
import static dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion.V1_16_4;
import static dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion.V1_17;
import static dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion.V1_17_1;
import static dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion.V1_18;
import static dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion.V1_18_2;
import static dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion.V1_19;
import static dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion.V1_19_1;
import static dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion.V1_19_3;
import static dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion.V1_19_4;
import static dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion.V1_20;
import static dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion.V1_20_2;
import static dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion.V1_20_3;
import static dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion.V1_20_5;
import static dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion.V1_21;
import static dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion.V1_21_11;
import static dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion.V1_21_2;
import static dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion.V1_21_4;
import static dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion.V1_21_5;
import static dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion.V1_21_6;
import static dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion.V1_21_7;
import static dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion.V1_21_9;
import static dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion.V1_7_10;
import static dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion.V1_7_2;
import static dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion.V1_8;
import static dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion.V1_9;
import static dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion.V1_9_1;
import static dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion.V1_9_2;
import static dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion.V1_9_4;
import static dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion.V26_1;

public class Util {

    public static TextComponentSerializer textComponentSerializerByVersion(int protocolVersion) {
        return switch (protocolVersion) {
            case V1_7_2, V1_7_10 -> TextComponentSerializer.V1_7;
            case V1_8 -> TextComponentSerializer.V1_8;
            case V1_9, V1_9_1, V1_9_2, V1_9_4, V1_10, V1_11, V1_11_1 -> TextComponentSerializer.V1_9;
            case V1_12, V1_12_1, V1_12_2, V1_13, V1_13_1, V1_13_2 -> TextComponentSerializer.V1_12;
            case V1_14, V1_14_1, V1_14_2, V1_14_3, V1_14_4 -> TextComponentSerializer.V1_14;
            case V1_15, V1_15_1, V1_15_2 -> TextComponentSerializer.V1_15;
            case V1_16, V1_16_1, V1_16_2, V1_16_3, V1_16_4 -> TextComponentSerializer.V1_16;
            case V1_17, V1_17_1 -> TextComponentSerializer.V1_17;
            case V1_18, V1_18_2, V1_19, V1_19_1, V1_19_3 -> TextComponentSerializer.V1_18;
            case V1_19_4, V1_20, V1_20_2 -> TextComponentSerializer.V1_19_4;
            case V1_20_3 -> TextComponentSerializer.V1_20_3;
            case V1_20_5, V1_21 -> TextComponentSerializer.V1_20_5;
            case V1_21_2 -> TextComponentSerializer.V1_21_2;
            case V1_21_4 -> TextComponentSerializer.V1_21_4;
            case V1_21_5 -> TextComponentSerializer.V1_21_5;
            case V1_21_6, V1_21_7 -> TextComponentSerializer.V1_21_6;
            case V1_21_9, V1_21_11, V26_1 -> TextComponentSerializer.V1_21_9;
            default -> TextComponentSerializer.LATEST;
        };
    }

    public static TextComponentCodec textComponentCodecByVersion(int protocolVersion) {
        return switch (protocolVersion) {
            case V1_20_3 -> TextComponentCodec.V1_20_3;
            case V1_20_5, V1_21 -> TextComponentCodec.V1_20_5;
            case V1_21_2 -> TextComponentCodec.V1_21_2;
            case V1_21_4 -> TextComponentCodec.V1_21_4;
            case V1_21_5 -> TextComponentCodec.V1_21_5;
            case V1_21_6, V1_21_7 -> TextComponentCodec.V1_21_6;
            case V1_21_9, V1_21_11, V26_1 -> TextComponentCodec.V1_21_9;
            default -> TextComponentCodec.LATEST;
        };
    }

    public static String sanitizeAddress(InetSocketAddress addr) {
        if (addr.isUnresolved()) {
            throw new IllegalStateException("Unresolved address");
        }
        String string = addr.getAddress().getHostAddress();
        if (addr.getAddress() instanceof Inet6Address) {
            int strip = string.indexOf('%');
            return (strip == -1) ? string : string.substring(0, strip);
        } else {
            return string;
        }
    }


}
