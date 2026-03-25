package dev.outfluencer.mcproxy.networking.netty.handler;

import dev.outfluencer.mcproxy.networking.protocol.packets.Packet;
import dev.outfluencer.mcproxy.networking.protocol.registry.PacketRegistry;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PacketEncoder extends MessageToByteEncoder<Packet<?>> {

    private int protocolVersion;
    private PacketRegistry registry;

    public PacketEncoder(int protocolVersion, PacketRegistry registry) {
        this.protocolVersion = protocolVersion;
        this.registry = registry;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet<?> packet, ByteBuf out) {
        int packetId = registry.getPacketId(protocolVersion, packet.getClass());
        if (packetId == -1) {
            throw new EncoderException("No packet ID for " + packet.getClass().getSimpleName() + " at version " + protocolVersion + " in state " + registry.getProtocol());
        }
        Packet.writeVarInt(packetId, out);
        packet.write(out, protocolVersion);
    }
}
