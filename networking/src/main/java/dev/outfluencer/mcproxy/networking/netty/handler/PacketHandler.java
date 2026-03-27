package dev.outfluencer.mcproxy.networking.netty.handler;

import dev.outfluencer.mcproxy.networking.ConnectionHandle;
import dev.outfluencer.mcproxy.networking.protocol.DecodedPacket;
import dev.outfluencer.mcproxy.networking.protocol.PacketListener;
import dev.outfluencer.mcproxy.networking.protocol.packets.Packet;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.logging.Level;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class PacketHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = Logger.getLogger(PacketHandler.class.getName());

    @Getter
    @Setter
    @NonNull
    private PacketListener packetHandler;
    private final ConnectionHandle connectionHandle;

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.log(Level.SEVERE, packetHandler + " caught exception: ", cause);
        try {
            packetHandler.onException(cause);
        } finally {
            connectionHandle.close(null);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        packetHandler.onConnect();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        connectionHandle.markClosed();
        packetHandler.onDisconnect();
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        packetHandler.onWritabilityChanged();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof DecodedPacket decodedPacket) {
            try {
                boolean sendPacket = true;
                Packet packet = decodedPacket.packet();
                if (packet != null) {
                    logger.info(packetHandler + " handles: " + packet);
                    if (packet.nextProtocol() != null) {
                        connectionHandle.setDecoderProtocol(packet.nextProtocol());
                    }
                    sendPacket = packet.handle(packetHandler);
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
}
