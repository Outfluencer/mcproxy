package dev.outfluencer.mcproxy.networking.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import javax.crypto.Cipher;
import javax.crypto.ShortBufferException;
import java.nio.ByteBuffer;
import java.util.List;

public final class CipherDecoder extends MessageToMessageDecoder<ByteBuf> {

    private final Cipher cipher;

    public CipherDecoder(Cipher cipher) {
        this.cipher = cipher;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws ShortBufferException {
        ByteBuffer nioBuffer = msg.nioBuffer();
        cipher.update(nioBuffer, nioBuffer.duplicate());
        out.add(msg.retain());
    }
}
