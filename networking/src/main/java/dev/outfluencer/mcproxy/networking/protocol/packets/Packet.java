package dev.outfluencer.mcproxy.networking.protocol.packets;

import dev.outfluencer.mcproxy.networking.Property;
import dev.outfluencer.mcproxy.networking.Util;
import dev.outfluencer.mcproxy.networking.protocol.PacketListener;
import dev.outfluencer.mcproxy.networking.protocol.registry.Protocol;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.lenni0451.mcstructs.nbt.NbtTag;
import net.lenni0451.mcstructs.nbt.NbtType;
import net.lenni0451.mcstructs.nbt.io.NbtReadTracker;
import net.lenni0451.mcstructs.nbt.io.impl.v1_12.NbtReader_v1_12;
import net.lenni0451.mcstructs.nbt.io.impl.v1_12.NbtWriter_v1_12;
import net.lenni0451.mcstructs.text.TextComponent;
import net.lenni0451.mcstructs.text.serializer.TextComponentCodec;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public abstract class Packet<T extends PacketListener> {

    private static void checkRead(int len, int max, String msg) {
        if (len < 0) {
            throw new DecoderException(msg + ", negative length " + len);
        }
        if (len > max) {
            throw new DecoderException(msg + ", length" + len + " exeeded maximum " + max);
        }
    }

    public static int readVarInt(ByteBuf input) {
        return readVarInt(input, 5);
    }

    public static String readString(ByteBuf input) {
        return readString(input, Short.MAX_VALUE);
    }

    public static String readString(ByteBuf input, int maxLength) {
        int len = readVarInt(input);
        checkRead(len, maxLength * 3, "read string bytes");
        String string = input.readString(len, StandardCharsets.UTF_8);
        checkRead(len, string.length(), "read string chars");
        return string;
    }

    public static byte[] readArray(ByteBuf buf)
    {
        return readArray( buf, buf.readableBytes() );
    }

    public static byte[] readArray(ByteBuf buf, int limit)
    {
        int len = readVarInt( buf );
        checkRead(len, limit, "read byte array");
        byte[] ret = new byte[ len ];
        buf.readBytes( ret );
        return ret;
    }

    public static int readVarInt(ByteBuf input, int maxBytes) {
        int out = 0;
        int bytes = 0;

        byte in;
        do {
            in = input.readByte();
            out |= (in & 127) << bytes++ * 7;
            checkRead(bytes, maxBytes, "varint read");
        } while ((in & 128) == 128);

        return out;
    }

    public static UUID readUUID(ByteBuf input) {
        return new UUID(input.readLong(), input.readLong());
    }

    public static Property[] readProperties(ByteBuf buf) {
        int size = readVarInt(buf);
        checkRead(size, buf.readableBytes(), "properties read");
        Property[] properties = new Property[size];
        for (int j = 0; j < properties.length; j++) {
            String name = readString(buf);
            String value = readString(buf);
            if (buf.readBoolean()) {
                properties[j] = new Property(name, value, readString(buf));
            } else {
                properties[j] = new Property(name, value);
            }
        }
        return properties;
    }

    public static TextComponent readComponent(ByteBuf byteBuf, int version) {
        TextComponentCodec codec = Util.textComponentCodecByVersion(version);
        return codec.deserialize(readTag(byteBuf, version));
    }

    public static NbtTag readTag(ByteBuf input, int protocolVersion, NbtReadTracker limiter) {
        DataInputStream in = new DataInputStream(new ByteBufInputStream(input));
        try {
            byte type = in.readByte();
            if (type == 0) {
                return null;
            } else {
                return new NbtReader_v1_12().read(NbtType.byId(type), in, limiter);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Exception reading tag", ex);
        }
    }

    public static NbtTag readTag(ByteBuf input, int protocolVersion) {
        return readTag(input, protocolVersion, new NbtReadTracker(1 << 21));
    }

    public static int[] readVarIntArray(ByteBuf buf) {
        int len = readVarInt(buf);
        checkRead(len, buf.readableBytes(), "properties read");
        int[] ret = new int[len];

        for (int i = 0; i < len; i++) {
            ret[i] = readVarInt(buf);
        }
        return ret;
    }

    public static void writeString(String string, ByteBuf output) {
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        writeVarInt(bytes.length, output);
        output.writeBytes(bytes);
    }

    public static void writeVarInt(int value, final ByteBuf output) {
        while ((value & -128) != 0) {
            output.writeByte(value & 127 | 128);
            value >>>= 7;
        }
        output.writeByte(value);
    }

    public static void writeUUID(UUID value, ByteBuf output) {
        output.writeLong(value.getMostSignificantBits());
        output.writeLong(value.getLeastSignificantBits());
    }

    public static void writeProperties(Property[] properties, ByteBuf buf) {
        if (properties == null) {
            writeVarInt(0, buf);
            return;
        }
        writeVarInt(properties.length, buf);
        for (Property prop : properties) {
            writeString(prop.getName(), buf);
            writeString(prop.getValue(), buf);
            if (prop.getSignature() != null) {
                buf.writeBoolean(true);
                writeString(prop.getSignature(), buf);
            } else {
                buf.writeBoolean(false);
            }
        }
    }

    public static void writeBaseComponent(TextComponent message, ByteBuf buf, int version) {
        TextComponentCodec codec = Util.textComponentCodecByVersion(version);
        writeTag(codec.serializeNbtTree(message), buf, version);
    }

    public static void writeTag(NbtTag tag, ByteBuf output, int protocolVersion) {
        DataOutputStream out = new DataOutputStream(new ByteBufOutputStream(output));
        try {
            out.writeByte(tag.getNbtType().getId());
            new NbtWriter_v1_12().write(out, tag);
        } catch (IOException ex) {
            throw new RuntimeException("Exception writing tag", ex);
        }
    }

    public static void writeVarIntArray(int[] arr, ByteBuf buf) {
        writeVarInt(arr.length, buf);
        for (int value : arr) {
            writeVarInt(value, buf);
        }
    }
    public static void writeArray(byte[] b, ByteBuf buf)
    {
        writeVarInt( b.length, buf );
        buf.writeBytes( b );
    }

    public abstract void read(ByteBuf byteBuf, int version);

    public abstract void write(ByteBuf byteBuf, int version);

    public abstract boolean handle(T listener);

    /**
     * If a packet is sent that changes the protocol phase, this method should
     * return the next protocol phase, it will be applied as the encoder protocol.
     * <p>
     * If a packet is received that changes protocol phase, this method should
     * return the next protocol phase, it will be applied as the decoder protocol.
     *
     * @return the next protocol tp apply
     */
    public Protocol nextProtocol() {
        return null;
    }
}
