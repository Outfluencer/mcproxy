package dev.outfluencer.mcproxy.networking.netty.handler;

import dev.outfluencer.mcproxy.networking.protocol.DecodedPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.Packet;
import dev.outfluencer.mcproxy.networking.protocol.packets.common.ClientboundUpdateTagsPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.config.ClientboundRegistryDataPacket;
import dev.outfluencer.mcproxy.networking.protocol.registry.PacketRegistry;
import dev.outfluencer.mcproxy.networking.protocol.registry.Protocol;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
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
                try {
                    packet.read(in, protocolVersion);
                } catch (Exception e) {
                    throw new DecoderException("Error reading " + packet.getClass().getSimpleName() + " (" + packetId + ") in " + registry.getProtocol(), e);
                }
                if (in.isReadable()) {
                    throw new DecoderException("Packet (" + packet.getClass() + ", "+ packetId + ") did not read to end: " + in.readableBytes());
                }
                if (packet instanceof ClientboundRegistryDataPacket
                 || packet instanceof ClientboundUpdateTagsPacket) {
                    verifyRoundTrip(ctx, packet, packetId, slice);
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

    private void verifyRoundTrip(ChannelHandlerContext ctx, Packet<?> packet, int packetId, ByteBuf originalFrame) {
        ByteBuf re = ctx.alloc().buffer(originalFrame.readableBytes());
        try {
            Packet.writeVarInt(packetId, re);
            packet.write(re, protocolVersion);

            ByteBuf orig = originalFrame.duplicate();
            int diff = firstDiff(orig, re);
            if (diff < 0) return;

            int origLen = orig.readableBytes();
            int reLen = re.readableBytes();
            int ctxStart = Math.max(0, diff - 16);
            int origCtxLen = Math.min(64, origLen - ctxStart);
            int reCtxLen = Math.min(64, reLen - ctxStart);

            System.err.printf(
                "ROUND-TRIP MISMATCH in %s at byte %d (orig len=%d, re len=%d)%n" +
                "  orig[%d..]: %s%n" +
                "  re  [%d..]: %s%n",
                packet.getClass().getSimpleName(), diff,
                origLen, reLen,
                ctxStart, ByteBufUtil.hexDump(orig, orig.readerIndex() + ctxStart, origCtxLen),
                ctxStart, ByteBufUtil.hexDump(re,   re.readerIndex()   + ctxStart, reCtxLen));
        } finally {
            re.release();
        }
    }

    private static int firstDiff(ByteBuf a, ByteBuf b) {
        int n = Math.min(a.readableBytes(), b.readableBytes());
        int ai = a.readerIndex();
        int bi = b.readerIndex();
        for (int i = 0; i < n; i++) {
            if (a.getByte(ai + i) != b.getByte(bi + i)) return i;
        }
        return a.readableBytes() == b.readableBytes() ? -1 : n;
    }

}
