package dev.outfluencer.mcproxy.networking.protocol.packets.handshake;

import dev.outfluencer.mcproxy.networking.protocol.packets.Packet;
import dev.outfluencer.mcproxy.networking.protocol.registry.Protocol;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ServerboundHandshakePacket extends Packet<ServerboundHandshakePacketListener> {

    private int version;
    private String hostName;
    private int port;
    private ClientIntent clientIntent;

    @Override
    public void read(ByteBuf byteBuf, int protocolVersion) {
        version = readVarInt(byteBuf);
        hostName = readString(byteBuf);
        port = byteBuf.readUnsignedShort();
        clientIntent = ClientIntent.byId(readVarInt(byteBuf));
    }

    @Override
    public void write(ByteBuf byteBuf, int protocolVersion) {
        writeVarInt(version, byteBuf);
        writeString(hostName, byteBuf);
        byteBuf.writeShort(port);
        writeVarInt(clientIntent.id(), byteBuf);
    }

    @Override
    public boolean handle(ServerboundHandshakePacketListener listener) {
        return listener.handle(this);
    }

    public enum ClientIntent {
        STATUS, LOGIN, TRANSFER;

        public static ClientIntent byId(final int id) {
            return switch (id) {
                case 1 -> STATUS;
                case 2 -> LOGIN;
                case 3 -> TRANSFER;
                default -> throw new IllegalArgumentException("Unknown connection intent: " + id);
            };
        }

        public int id() {
            return switch (this) {
                case STATUS -> 1;
                case LOGIN -> 2;
                case TRANSFER -> 3;
            };
        }

        public boolean isLogin() {
            return this == LOGIN || this == TRANSFER;
        }
    }
}
