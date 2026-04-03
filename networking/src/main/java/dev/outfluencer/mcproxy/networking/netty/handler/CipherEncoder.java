package dev.outfluencer.mcproxy.networking.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.MessageToMessageEncoder;

import javax.crypto.Cipher;
import javax.crypto.ShortBufferException;
import java.nio.ByteBuffer;
import java.util.List;

public final class CipherEncoder extends MessageToMessageEncoder<ByteBuf> {

    private final Cipher cipher;

    public CipherEncoder(Cipher cipher) {
        this.cipher = cipher;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        ByteBuffer buffer = msg.nioBuffer();
        cipher.update(buffer, buffer.duplicate());
        out.add(msg.retain());
    }
}
