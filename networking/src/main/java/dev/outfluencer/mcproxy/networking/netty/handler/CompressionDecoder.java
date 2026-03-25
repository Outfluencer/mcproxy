package dev.outfluencer.mcproxy.networking.netty.handler;


import dev.outfluencer.mcproxy.networking.protocol.packets.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.zip.Inflater;

@Getter
@Setter
public final class CompressionDecoder extends ByteToMessageDecoder {

    public static final int MAXIMUM_COMPRESSED_LENGTH = 2097152;
    public static final int MAXIMUM_UNCOMPRESSED_LENGTH = 8388608;

    private final Inflater inflater = new Inflater();
    private int threshold;

    public CompressionDecoder(int threshold) {
        this.threshold = threshold;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int uncompressedSize = Packet.readVarInt(in);
        if (uncompressedSize == 0) {
            // data is not compressed, pass through as-is
            out.add(in.readBytes(in.readableBytes()));
            return;
        }
        if (uncompressedSize < threshold) {
            throw new DecoderException("Badly compressed packet - size of " + uncompressedSize + " is below server threshold of " + threshold);
        }
        if (uncompressedSize > MAXIMUM_UNCOMPRESSED_LENGTH) {
            throw new DecoderException("Badly compressed packet - size of " + uncompressedSize + " is larger than protocol maximum of " + MAXIMUM_UNCOMPRESSED_LENGTH);
        }
        int compressedSize = in.readableBytes();
        if (compressedSize > MAXIMUM_COMPRESSED_LENGTH) {
            throw new DecoderException("Badly compressed packet - compressed size of " + compressedSize + " is larger than maximum of " + MAXIMUM_COMPRESSED_LENGTH);
        }

        ByteBuf output = ctx.alloc().directBuffer(uncompressedSize);
        try {
            inflater.setInput(in.nioBuffer());
            output.writerIndex(output.writerIndex() + inflater.inflate(output.nioBuffer(output.writerIndex(), uncompressedSize)));
            inflater.reset();
            in.skipBytes(compressedSize);

            if (output.readableBytes() != uncompressedSize) {
                throw new DecoderException("Badly compressed packet - actual size of " + output.readableBytes() + " does not match claimed size of " + uncompressedSize);
            }

            out.add(output);
            output = null; // prevent release in finally
        } finally {
            if (output != null) {
                output.release();
            }
        }
    }
}
