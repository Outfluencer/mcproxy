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
import io.netty.channel.ChannelFuture;
import io.netty.util.AttributeKey;
import lombok.Getter;

public final class ConnectionHandle {

    public static final AttributeKey<ConnectionHandle> ATTRIBUTE_KEY = AttributeKey.newInstance("ConnectionHandle");

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
        this.closed = true;
    }


    public ConnectionHandle(Channel channel, boolean server) {
        assert channel.eventLoop().inEventLoop();
        this.channel = channel;
        this.channel.attr(ATTRIBUTE_KEY).set(this);
        this.server = server;
    }

    public void sendPackets(Packet<?>... packets) {
        assert channel.eventLoop().inEventLoop();
        for (Packet<?> packet : packets) {
            channel.write(packet, channel.voidPromise());
        }
        channel.flush();
    }

    public void sendPacket(Packet<?> packet) {
        assert channel.eventLoop().inEventLoop();
        channel.writeAndFlush(packet, channel.voidPromise());
    }

    public void sendDecodedPacket(DecodedPacket decodedPacket) {
        channel.writeAndFlush(decodedPacket.byteBuf().retain(), channel.voidPromise());
    }

    public ChannelFuture sendPacketAndAwait(Packet<?> packet) {
        assert channel.eventLoop().inEventLoop();
        return channel.writeAndFlush(packet);
    }

    public void secureClose(Packet<?> packet) {
        assert channel.eventLoop().inEventLoop();
        if (closed) {
            return;
        }
        setClosing();
        channel.writeAndFlush(packet == null ? Unpooled.EMPTY_BUFFER : packet).addListener(_ -> channel.close());
    }

    public void secureCloseAndThen(Packet<?> packet, Runnable runnable) {
        assert channel.eventLoop().inEventLoop();
        if (closed) {
            return;
        }
        setClosing();
        channel.writeAndFlush(packet == null ? Unpooled.EMPTY_BUFFER : packet).addListener(_ -> {
            channel.close().addListener(_ -> runnable.run());

        });
    }

    public void forceClose(Packet<?> packet) {
        assert channel.eventLoop().inEventLoop();
        if (closed) {
            return;
        }
        setClosing();
        if (packet != null) {
            channel.write(packet, channel.voidPromise());
        }
        channel.flush();
        channel.close();
    }

    public void setAutoRead(boolean autoRead) {
        assert channel.eventLoop().inEventLoop();
        if (closed) {
            return;
        }
        channel.config().setAutoRead(autoRead);
    }

    public void setClosing() {
        assert channel.eventLoop().inEventLoop();
        //assert !closing : "closing called twice";
        if (closed) {
            return;
        }
        closed = true;
        channel.config().setAutoRead(false);
    }
}
