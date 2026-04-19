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
public class ClientboundLoginPacket extends Packet<ClientboundGamePacketListener> {

    private int entityId;
    private boolean hardcore;
    private String[] dimensions;
    private int maxPlayers;
    private int viewDistance;
    private int simulationDistance;
    private boolean reducedDebugInfo;
    private boolean enableRespawnScreen;
    private boolean doLimitedCrafting;
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
    private boolean enforcesSecureChat;

    @Override
    public void read(ByteBuf byteBuf, int version) {
        entityId = byteBuf.readInt();
        hardcore = byteBuf.readBoolean();
        int dimensionCount = readVarInt(byteBuf);
        dimensions = new String[dimensionCount];
        for (int i = 0; i < dimensionCount; i++) {
            dimensions[i] = readString(byteBuf);
        }
        maxPlayers = readVarInt(byteBuf);
        viewDistance = readVarInt(byteBuf);
        simulationDistance = readVarInt(byteBuf);
        reducedDebugInfo = byteBuf.readBoolean();
        enableRespawnScreen = byteBuf.readBoolean();
        doLimitedCrafting = byteBuf.readBoolean();
        dimensionType = readVarInt(byteBuf);
        dimensionId = readString(byteBuf);
        seedHash = byteBuf.readLong();
        gamemode = byteBuf.readUnsignedByte();
        previousGamemode = byteBuf.readByte();
        debug = byteBuf.readBoolean();
        flat = byteBuf.readBoolean();
        hasDeath = byteBuf.readBoolean();
        if (hasDeath) {
            deathDimension = readString(byteBuf);
            deathLocation = byteBuf.readLong();
        }
        portalCooldown = readVarInt(byteBuf);
        seaLevel = readVarInt(byteBuf);
        enforcesSecureChat = byteBuf.readBoolean();
    }

    @Override
    public void write(ByteBuf byteBuf, int version) {
        byteBuf.writeInt(entityId);
        byteBuf.writeBoolean(hardcore);
        writeVarInt(dimensions.length, byteBuf);
        for (String dimension : dimensions) {
            writeString(dimension, byteBuf);
        }
        writeVarInt(maxPlayers, byteBuf);
        writeVarInt(viewDistance, byteBuf);
        writeVarInt(simulationDistance, byteBuf);
        byteBuf.writeBoolean(reducedDebugInfo);
        byteBuf.writeBoolean(enableRespawnScreen);
        byteBuf.writeBoolean(doLimitedCrafting);
        writeVarInt(dimensionType, byteBuf);
        writeString(dimensionId, byteBuf);
        byteBuf.writeLong(seedHash);
        byteBuf.writeByte(gamemode);
        byteBuf.writeByte(previousGamemode);
        byteBuf.writeBoolean(debug);
        byteBuf.writeBoolean(flat);
        byteBuf.writeBoolean(hasDeath);
        if (hasDeath) {
            writeString(deathDimension, byteBuf);
            byteBuf.writeLong(deathLocation);
        }
        writeVarInt(portalCooldown, byteBuf);
        writeVarInt(seaLevel, byteBuf);
        byteBuf.writeBoolean(enforcesSecureChat);
    }

    @Override
    public boolean handle(ClientboundGamePacketListener listener) {
        return listener.handle(this);
    }
}
