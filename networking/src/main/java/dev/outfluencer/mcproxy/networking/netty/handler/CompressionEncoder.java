package dev.outfluencer.mcproxy.networking.netty.handler;

import dev.outfluencer.mcproxy.networking.protocol.packets.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.Getter;
import lombok.Setter;

import java.util.zip.Deflater;

@Getter
@Setter
public final class CompressionEncoder extends MessageToByteEncoder<ByteBuf> {

    public static final int MAXIMUM_COMPRESSED_LENGTH = 2097152;
    public static final int MAXIMUM_UNCOMPRESSED_LENGTH = 8388608;

    private final Deflater deflater = new Deflater();
    private int threshold;

    public CompressionEncoder(int threshold) {
        this.threshold = threshold;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
        int uncompressedSize = msg.readableBytes();
        if (uncompressedSize < threshold) {
            // below threshold: write 0 as data length (uncompressed) followed by raw data
            Packet.writeVarInt(0, out);
            out.writeBytes(msg);
            return;
        }

        // write the uncompressed data length
        Packet.writeVarInt(uncompressedSize, out);

        // compress directly from/to NIO buffers — no byte[] copies
        deflater.setInput(msg.nioBuffer());
        deflater.finish();

        while (!deflater.finished()) {
            out.ensureWritable(8192);
            int writerIndex = out.writerIndex();
            int count = deflater.deflate(out.nioBuffer(writerIndex, out.writableBytes()));
            out.writerIndex(writerIndex + count);
        }
        deflater.reset();
    }

    @Override
    protected ByteBuf allocateBuffer(ChannelHandlerContext ctx, ByteBuf msg, boolean preferDirect) {
        return ctx.alloc().directBuffer(5 + msg.readableBytes());
    }
}
