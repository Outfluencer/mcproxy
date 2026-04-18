package dev.outfluencer.mcproxy.networking.protocol.packets.game;

import dev.outfluencer.mcproxy.networking.protocol.packets.Packet;
import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ClientboundRespawnPacket extends Packet<ClientboundGamePacketListener> {

    private int dimensionType;
    private String dimensionId;
    private long seedHash;
    private int gamemode;
    private int previousGamemode;
    private boolean debug;
    private boolean flat;

    private boolean hasDeath;
    private String deathDimension;
    private long deathLocation;

    private int portalCooldown;
    private int seaLevel;
    private int keepData;


    @Override
    public void read(ByteBuf byteBuf, int version) {
        dimensionType = readVarInt(byteBuf);
        dimensionId = readString(byteBuf);
        seedHash = byteBuf.readLong();
        gamemode = byteBuf.readUnsignedByte();
        previousGamemode = byteBuf.readByte();
        debug = byteBuf.readBoolean();
        flat = byteBuf.readBoolean();
        hasDeath = byteBuf.readBoolean();
        if(hasDeath) {
            deathDimension = readString(byteBuf);
            deathLocation = byteBuf.readLong();
        }
        portalCooldown = readVarInt(byteBuf);
        seaLevel = readVarInt(byteBuf);
        keepData = byteBuf.readByte();
    }

    @Override
    public void write(ByteBuf byteBuf, int version) {
        writeVarInt(dimensionType, byteBuf);
        writeString(dimensionId, byteBuf);
        byteBuf.writeLong(seedHash);
        byteBuf.writeByte(gamemode);
        byteBuf.writeByte(previousGamemode);
        byteBuf.writeBoolean(debug);
        byteBuf.writeBoolean(flat);
        byteBuf.writeBoolean(hasDeath);
        if(hasDeath) {
            writeString(deathDimension, byteBuf);
            byteBuf.writeLong(deathLocation);
        }
        writeVarInt(portalCooldown, byteBuf);
        writeVarInt(seaLevel, byteBuf);
        byteBuf.writeByte(keepData);
    }

    @Override
    public boolean handle(ClientboundGamePacketListener listener) {
        return listener.handle(this);
    }
}
