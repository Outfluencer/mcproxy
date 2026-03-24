package dev.outfluencer.mcproxy.networking.protocol;

import dev.outfluencer.mcproxy.networking.protocol.packets.Packet;
import dev.outfluencer.mcproxy.networking.protocol.registry.Protocol;
import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCounted;

public record DecodedPacket(Packet packet, ByteBuf byteBuf, Protocol protocol) implements ReferenceCounted {

    @Override
    public int refCnt() {
        return byteBuf.refCnt();
    }

    @Override
    public ReferenceCounted retain() {
        byteBuf.retain();
        return this;
    }

    @Override
    public ReferenceCounted retain(int i) {
        byteBuf.retain(i);
        return this;
    }

    @Override
    public ReferenceCounted touch() {
        byteBuf.touch();
        return this;
    }

    @Override
    public ReferenceCounted touch(Object o) {
        byteBuf.touch(o);
        return this;
    }

    @Override
    public boolean release() {
        return byteBuf.release();
    }

    @Override
    public boolean release(int i) {
        return byteBuf.release(i);
    }
}
