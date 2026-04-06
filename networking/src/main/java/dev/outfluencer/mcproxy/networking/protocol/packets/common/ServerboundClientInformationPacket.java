package dev.outfluencer.mcproxy.networking.protocol.packets.common;

import dev.outfluencer.mcproxy.networking.protocol.packets.Packet;
import dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ServerboundClientInformationPacket extends Packet<ServerboundCommonPacketListener> {
    private String locale;
    private byte viewDistance;
    private int chatFlags;
    private boolean chatColours;
    private byte difficulty;
    private byte skinParts;
    private int mainHand;
    private boolean disableTextFiltering;
    private boolean allowServerListing;
    private ParticleStatus particleStatus;
    @Override
    public void read(ByteBuf buf, int protocolVersion) {
        locale = readString(buf, 16);
        viewDistance = buf.readByte();
        chatFlags = readVarInt(buf);
        chatColours = buf.readBoolean();
        skinParts = buf.readByte();
        mainHand = readVarInt(buf);
        disableTextFiltering = buf.readBoolean();
        allowServerListing = buf.readBoolean();
        if (protocolVersion >= MinecraftVersion.V1_21_2) {
            particleStatus = ParticleStatus.values()[readVarInt(buf)];
        }
    }

    @Override
    public void write(ByteBuf byteBuf, int version) {
        writeString(locale, byteBuf);
        byteBuf.writeByte(viewDistance);
        byteBuf.writeByte(chatFlags);
        byteBuf.writeBoolean(chatColours);
        byteBuf.writeByte(skinParts);
        byteBuf.writeByte(mainHand);
        byteBuf.writeBoolean(disableTextFiltering);
        byteBuf.writeBoolean(allowServerListing);
        if (version >= MinecraftVersion.V1_21_2) {
            writeVarInt(particleStatus.ordinal(), byteBuf);
        }
    }

    @Override
    public boolean handle(ServerboundCommonPacketListener listener) {
        return listener.handle(this);
    }


    public enum ParticleStatus {
        ALL, DECREASED, MINIMAL;
    }
}
