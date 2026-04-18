package dev.outfluencer.mcproxy.networking.protocol.packets.login;

import dev.outfluencer.mcproxy.networking.protocol.packets.Packet;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ClientboundLoginEncryptionRequestPacket extends Packet<ClientboundLoginPacketListener> {

    private String serverId;
    private byte[] publicKey;
    private byte[] verifyToken;
    private boolean shouldAuthenticate;

    @Override
    public void read(ByteBuf byteBuf, int version) {
        serverId = readString(byteBuf);
        publicKey = readArray(byteBuf);
        verifyToken = readArray(byteBuf);
        shouldAuthenticate = byteBuf.readBoolean();
    }

    @Override
    public void write(ByteBuf byteBuf, int version) {
        writeString(serverId, byteBuf);
        writeArray(publicKey, byteBuf);
        writeArray(verifyToken, byteBuf);
        byteBuf.writeBoolean(shouldAuthenticate);
    }

    @Override
    public boolean handle(ClientboundLoginPacketListener listener) {
        return listener.handle(this);
    }
}
