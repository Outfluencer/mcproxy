package dev.outfluencer.mcproxy.networking.netty;

import dev.outfluencer.mcproxy.networking.ConnectionHandle;
import dev.outfluencer.mcproxy.networking.protocol.DecodedPacket;
import dev.outfluencer.mcproxy.networking.protocol.PacketListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@AllArgsConstructor
public class PacketHandler extends ChannelInboundHandlerAdapter {

    @Setter
    @NonNull
    private PacketListener packetHandler;
    private final ConnectionHandle connectionHandle;

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        packetHandler.onException(cause);
        ctx.close();
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
                if (decodedPacket.packet() != null) {
                    sendPacket = decodedPacket.packet().handle(packetHandler);
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
