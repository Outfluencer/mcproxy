package dev.outfluencer.mcproxy.networking;

import dev.outfluencer.mcproxy.networking.netty.ClearSignal;
import dev.outfluencer.mcproxy.networking.netty.HandlerNames;
import dev.outfluencer.mcproxy.networking.netty.handler.CompressionDecoder;
import dev.outfluencer.mcproxy.networking.netty.handler.CompressionEncoder;
import dev.outfluencer.mcproxy.networking.netty.handler.PacketDecoder;
import dev.outfluencer.mcproxy.networking.netty.handler.PacketEncoder;
import dev.outfluencer.mcproxy.networking.netty.handler.PacketHandler;
import dev.outfluencer.mcproxy.networking.protocol.DecodedPacket;
import dev.outfluencer.mcproxy.networking.protocol.PacketListener;
import dev.outfluencer.mcproxy.networking.protocol.packets.Packet;
import dev.outfluencer.mcproxy.networking.protocol.registry.Protocol;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoop;
import lombok.Getter;

public final class ConnectionHandle {

    @Getter
    private final Channel channel;
    private final PacketDecoder decoder;
    private final PacketEncoder encoder;
    private final boolean server;

    @Getter
    private int protocolVersion;
    @Getter
    private boolean closed;

    public void markClosed() {
        assert channel.eventLoop().inEventLoop();
        this.closed = true;
    }

    public ConnectionHandle(Channel channel, boolean server) {
        assert channel.eventLoop().inEventLoop();
        // cache those values
        this.decoder = channel.pipeline().get(PacketDecoder.class);
        this.encoder = channel.pipeline().get(PacketEncoder.class);
        this.channel = channel;
        this.server = server;
    }

    public void sendPacket(Packet<?> packet) {
        assert channel.eventLoop().inEventLoop();
        if (closed) {
            return;
        }
        channel.writeAndFlush(packet, channel.voidPromise());
        if (packet.nextProtocol() != null) {
            setEncoderProtocol(packet.nextProtocol());
        }
    }

    public void sendDecodedPacket(DecodedPacket decodedPacket) {
        assert channel.eventLoop().inEventLoop();
        if (closed || decodedPacket.protocol() != getEncoderProtocol()) {
            return;
        }
        channel.writeAndFlush(decodedPacket.byteBuf().retain(), channel.voidPromise());

        Packet<?> packet = decodedPacket.packet();
        if (packet != null && packet.nextProtocol() != null) {
            setEncoderProtocol(packet.nextProtocol());
        }
    }

    public void close(Packet<?> packet) {
        assert channel.eventLoop().inEventLoop();
        // if the input is already shut down cancel here.
        if (closed) {
            return;
        }
        channel.config().setAutoRead(false);
        channel.pipeline().fireUserEventTriggered(ClearSignal.INSTANCE);
        channel.writeAndFlush(packet == null ? Unpooled.EMPTY_BUFFER : packet).addListener(ChannelFutureListener.CLOSE);
        closed = true;
    }

    public void setCompression(int threshold) {
        assert channel.eventLoop().inEventLoop();
        if (threshold >= 0) {
            if (this.channel.pipeline().get(HandlerNames.DECOMPRESS) instanceof CompressionDecoder compressionDecoder) {
                compressionDecoder.setThreshold(threshold);
            } else {
                this.channel.pipeline().addAfter(HandlerNames.SPLITTER, HandlerNames.DECOMPRESS, new CompressionDecoder(threshold));
            }

            if (this.channel.pipeline().get(HandlerNames.COMPRESS) instanceof CompressionEncoder compressionEncoder) {
                compressionEncoder.setThreshold(threshold);
            } else {
                this.channel.pipeline().addAfter(HandlerNames.PREPENDER, HandlerNames.COMPRESS, new CompressionEncoder(threshold));
            }
        } else {
            if (this.channel.pipeline().get(HandlerNames.DECOMPRESS) instanceof CompressionDecoder) {
                this.channel.pipeline().remove(HandlerNames.DECOMPRESS);
            }

            if (this.channel.pipeline().get(HandlerNames.COMPRESS) instanceof CompressionEncoder) {
                this.channel.pipeline().remove(HandlerNames.COMPRESS);
            }
        }
    }

    public void setProtocolVersion(int protocolVersion) {
        assert channel.eventLoop().inEventLoop();
        this.protocolVersion = protocolVersion;
        decoder.setProtocolVersion(protocolVersion);
        encoder.setProtocolVersion(protocolVersion);
    }

    public void setProtocol(Protocol protocol) {
        assert channel.eventLoop().inEventLoop();
        setDecoderProtocol(protocol);
        setEncoderProtocol(protocol);
    }

    public void setDecoderProtocol(Protocol protocol) {
        assert channel.eventLoop().inEventLoop();
        decoder.setRegistry(server ? protocol.clientbound : protocol.serverbound);
    }

    public void setEncoderProtocol(Protocol protocol) {
        assert channel.eventLoop().inEventLoop();
        encoder.setRegistry(server ? protocol.serverbound : protocol.clientbound);
    }

    public void setPacketListener(PacketListener listener) {
        assert channel.eventLoop().inEventLoop();
        channel.pipeline().get(PacketHandler.class).setPacketHandler(listener);
    }

    public PacketListener getPacketListener() {
        assert channel.eventLoop().inEventLoop();
        return channel.pipeline().get(PacketHandler.class).getPacketHandler();
    }

    public Protocol getEncoderProtocol() {
        assert channel.eventLoop().inEventLoop();
        return encoder.getRegistry().getProtocol();
    }

    public Protocol getDecoderProtocol() {
        assert channel.eventLoop().inEventLoop();
        return decoder.getRegistry().getProtocol();
    }

    public void runInEventLoop(Runnable runnable) {
        EventLoop eventLoop = channel.eventLoop();
        if (eventLoop.inEventLoop()) {
            runnable.run();
        } else {
            channel.eventLoop().submit(runnable).addListener(future -> {
                if (!future.isSuccess()) {
                    channel.pipeline().fireExceptionCaught(future.cause());
                }
            });
        }
    }

}
