package dev.outfluencer.mcproxy.networking.netty.handler;

import dev.outfluencer.mcproxy.networking.protocol.DecodedPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.Packet;
import dev.outfluencer.mcproxy.networking.protocol.registry.PacketRegistry;
import dev.outfluencer.mcproxy.networking.protocol.registry.Protocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Getter
@Setter
public class PacketDecoder extends MessageToMessageDecoder<ByteBuf> {

    private int protocolVersion;
    private PacketRegistry registry;

    public PacketDecoder(int protocolVersion, PacketRegistry registry) {
        this.protocolVersion = protocolVersion;
        this.registry = registry;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        ByteBuf slice = in.retainedSlice();
        try {
            int packetId = Packet.readVarInt(in);
            Packet<?> packet = registry.createPacket(protocolVersion, packetId);
            if (packet != null) {
                packet.read(in, protocolVersion);
                if (in.isReadable()) {
                    throw new DecoderException("Packet did not read to end");
                }
            } else {
                in.skipBytes(in.readableBytes());
            }
            out.add(new DecodedPacket(packet, slice, registry.getProtocol()));
            slice = null;
        } finally {
            if (slice != null) {
                slice.release();
            }
        }
    }

}
