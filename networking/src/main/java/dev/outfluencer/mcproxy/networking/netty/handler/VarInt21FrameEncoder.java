package dev.outfluencer.mcproxy.networking.netty.handler;

import dev.outfluencer.mcproxy.networking.protocol.packets.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

public class VarInt21FrameEncoder extends MessageToMessageEncoder<ByteBuf> {

    private static byte varIntSize(int value) {
        if ((value & 0xFFFFFF80) == 0) {
            return 1;
        }
        if ((value & 0xFFFFC000) == 0) {
            return 2;
        }
        if ((value & 0xFFE00000) == 0) {
            return 3;
        }
        if ((value & 0xF0000000) == 0) {
            return 4;
        }
        return 5;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> list) throws Exception {
        int bodyLength = msg.readableBytes();
        int headerLength = varIntSize(bodyLength);
        if (headerLength > 3) {
            throw new EncoderException("Packet too large: size " + bodyLength + " is over 8");
        }
        ByteBuf out = ctx.alloc().directBuffer(headerLength + bodyLength);
        Packet.writeVarInt(bodyLength, out);
        out.writeBytes(msg, msg.readerIndex(), bodyLength);
        list.add(out);
    }
}
