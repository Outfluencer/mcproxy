package dev.outfluencer.mcproxy.networking.netty.handler;

import dev.outfluencer.mcproxy.networking.ConnectionHandle;
import dev.outfluencer.mcproxy.networking.protocol.DecodedPacket;
import dev.outfluencer.mcproxy.networking.protocol.PacketListener;
import dev.outfluencer.mcproxy.networking.protocol.packets.Packet;
import dev.outfluencer.mcproxy.networking.protocol.registry.Protocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
public class PacketHandler extends ChannelInboundHandlerAdapter {

    @Getter
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
               {
                    // close the connection at random times to trigger a fallback and see if everything is correctly impled
                    if(connectionHandle.isServer() && ThreadLocalRandom.current().nextInt(50) == 1) {
                        connectionHandle.close(null);
                        return;
                    }
                }
                boolean sendPacket = true;
                Packet packet = decodedPacket.packet();
                if (packet != null) {
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
