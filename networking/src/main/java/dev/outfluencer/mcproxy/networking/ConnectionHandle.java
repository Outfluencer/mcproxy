package dev.outfluencer.mcproxy.networking;

import dev.outfluencer.mcproxy.networking.netty.PacketDecoder;
import dev.outfluencer.mcproxy.networking.netty.PacketEncoder;
import dev.outfluencer.mcproxy.networking.netty.PacketHandler;
import dev.outfluencer.mcproxy.networking.protocol.DecodedPacket;
import dev.outfluencer.mcproxy.networking.protocol.PacketListener;
import dev.outfluencer.mcproxy.networking.protocol.packets.Packet;
import dev.outfluencer.mcproxy.networking.protocol.registry.Protocol;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.Getter;

public final class ConnectionHandle {

    @Getter
    private int protocolVersion;
    private final boolean server;

    public void setProtocolVersion(int protocolVersion) {
        assert channel.eventLoop().inEventLoop();
        this.protocolVersion = protocolVersion;
        channel.pipeline().get(PacketDecoder.class).setProtocolVersion(protocolVersion);
        channel.pipeline().get(PacketEncoder.class).setProtocolVersion(protocolVersion);
    }


    public void setProtocol(Protocol protocol) {
        assert channel.eventLoop().inEventLoop();
        setDecoderProtocol(protocol);
        setEncoderProtocol(protocol);
    }

    public void setDecoderProtocol(Protocol protocol) {
        assert channel.eventLoop().inEventLoop();
        channel.pipeline().get(PacketDecoder.class).setRegistry(server ? protocol.clientbound : protocol.serverbound);
    }

    public void setEncoderProtocol(Protocol protocol) {
        assert channel.eventLoop().inEventLoop();
        channel.pipeline().get(PacketEncoder.class).setRegistry(server ? protocol.serverbound : protocol.clientbound);
    }

    public void setPacketListener(PacketListener listener) {
        assert channel.eventLoop().inEventLoop();
        channel.pipeline().get(PacketHandler.class).setPacketHandler(listener);
    }

    public Protocol getEncoderProtocol() {
        assert channel.eventLoop().inEventLoop();
        return channel.pipeline().get(PacketEncoder.class).getRegistry().getProtocol();
    }

    public Protocol getDecoderProtocol() {
        assert channel.eventLoop().inEventLoop();
        return channel.pipeline().get(PacketDecoder.class).getRegistry().getProtocol();
    }

    @Getter
    private final Channel channel;
    @Getter
    private boolean closed;

    public void markClosed() {
        assert channel.eventLoop().inEventLoop();
        this.closed = true;
    }

    public ConnectionHandle(Channel channel, boolean server) {
        assert channel.eventLoop().inEventLoop();
        this.channel = channel;
        this.server = server;
    }

    public void sendPacket(Packet<?> packet) {
        assert channel.eventLoop().inEventLoop();
        channel.writeAndFlush(packet, channel.voidPromise());
    }

    public void sendDecodedPacket(DecodedPacket decodedPacket) {
        assert channel.eventLoop().inEventLoop();
        if (closed || decodedPacket.protocol() != getEncoderProtocol()) {
            return;
        }
        channel.writeAndFlush(decodedPacket.byteBuf().retain(), channel.voidPromise());
    }

    public void close(Packet<?> packet) {
        assert channel.eventLoop().inEventLoop();
        // if the input is already shut down cancel here.
        if (closed) {
            return;
        }
        channel.config().setAutoRead(false);
        channel.writeAndFlush(packet == null ? Unpooled.EMPTY_BUFFER : packet).addListener(ChannelFutureListener.CLOSE);
        closed = true;
    }

    public void setAutoRead(boolean autoRead) {
        assert channel.eventLoop().inEventLoop();
        if (closed) {
            return;
        }
        channel.config().setAutoRead(autoRead);
    }

}
