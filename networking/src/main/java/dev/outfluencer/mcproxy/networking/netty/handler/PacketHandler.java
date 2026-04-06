package dev.outfluencer.mcproxy.networking.netty.handler;

import dev.outfluencer.mcproxy.networking.ConnectionHandle;
import dev.outfluencer.mcproxy.networking.netty.PacketLimiter;
import dev.outfluencer.mcproxy.networking.netty.QuietException;
import dev.outfluencer.mcproxy.networking.protocol.DecodedPacket;
import dev.outfluencer.mcproxy.networking.protocol.PacketListener;
import dev.outfluencer.mcproxy.networking.protocol.packets.Packet;
import dev.outfluencer.mcproxy.networking.protocol.registry.Protocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.TimeoutException;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.logging.Level;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class PacketHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = Logger.getLogger(PacketHandler.class.getName());

    @Setter
    private PacketLimiter packetLimiter;
    @Getter
    @Setter
    @NonNull
    private PacketListener packetHandler;
    private final ConnectionHandle connectionHandle;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        connectionHandle.setAddress();
        packetHandler.onConnect();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        connectionHandle.markClosed();
        packetHandler.onDisconnect();
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        connectionHandle.setAutoRead(ctx.channel().isWritable());
        packetHandler.onWritabilityChanged();
    }

    public void encoderProtocolChanged(Protocol protocol) {
        packetHandler.encoderProtocolChanged(protocol);
    }

    public void decoderProtocolChanged(Protocol protocol) {
        packetHandler.decoderProtocolChanged(protocol);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        assert !connectionHandle.isClosed() : "received data of closed channel";
        if (msg instanceof DecodedPacket decodedPacket) {
            try {
                boolean sendPacket = true;
                Packet packet = decodedPacket.packet();
                if (packet != null) {
                    if (packet.nextProtocol() != null) {
                        connectionHandle.setDecoderProtocol(packet.nextProtocol());
                    }
                        sendPacket = packet.handle(packetHandler);
                }

                if (packetLimiter != null && !packetLimiter.incrementAndCheck(decodedPacket.byteBuf().readableBytes())) {
                    throw new QuietException("Too many packets received");
                }

                if (sendPacket) {
                    packetHandler.handle(decodedPacket);
                }
            } finally {
                decodedPacket.release();
            }
        } else {
            throw new UnsupportedOperationException(String.format("%s is not supported", msg.getClass().getName()));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        connectionHandle.disconnect("§c" + cause);
        if (cause instanceof QuietException || cause instanceof TimeoutException) {
            logger.log(Level.WARNING, "{0} caught exception: {1}", new Object[] {packetHandler, cause});
        } else {
            logger.log(Level.SEVERE, packetHandler + " caught exception: ", cause);
        }
    }
}
