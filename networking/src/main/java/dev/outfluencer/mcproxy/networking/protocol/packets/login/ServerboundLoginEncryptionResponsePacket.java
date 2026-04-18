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
public class ServerboundLoginEncryptionResponsePacket extends Packet<ServerboundLoginPacketListener> {

    private byte[] sharedSecret;
    private byte[] verifyToken;

    @Override
    public void read(ByteBuf byteBuf, int version) {
        sharedSecret = readArray(byteBuf, 128);
        verifyToken = readArray(byteBuf, 128);
    }

    @Override
    public void write(ByteBuf byteBuf, int version) {
        writeArray(sharedSecret, byteBuf);
        writeArray(verifyToken, byteBuf);
    }

    @Override
    public boolean handle(ServerboundLoginPacketListener listener) {
        return listener.handle(this);
    }
}
