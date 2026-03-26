package dev.outfluencer.mcproxy.proxy;

import dev.outfluencer.mcproxy.networking.ConnectionHandle;
import dev.outfluencer.mcproxy.networking.netty.HandlerNames;
import dev.outfluencer.mcproxy.networking.netty.handler.PacketDecoder;
import dev.outfluencer.mcproxy.networking.netty.handler.PacketEncoder;
import dev.outfluencer.mcproxy.networking.netty.handler.PacketHandler;
import dev.outfluencer.mcproxy.networking.netty.PipelineUtil;
import dev.outfluencer.mcproxy.networking.netty.handler.Varint21FrameDecoder;
import dev.outfluencer.mcproxy.networking.netty.handler.VarInt21FrameEncoder;
import dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion;
import dev.outfluencer.mcproxy.networking.protocol.registry.Protocol;
import dev.outfluencer.mcproxy.proxy.connection.handler.PlayerHandshakePacketListener;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.util.ResourceLeakDetector;

public class MinecraftProxy {

    private static final int PORT = 25565;

    public static void start() {

        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);
        if ( System.getProperty( "io.netty.allocator.type" ) == null )
        {
            System.setProperty( "io.netty.allocator.type", "pooled" );
        }

        EventLoopGroup boss = PipelineUtil.newEventLoopGroup(1);
        EventLoopGroup worker = PipelineUtil.newEventLoopGroup(0);

        new ServerBootstrap().group(boss, worker).channel(PipelineUtil.serverChannelType()).childHandler(new ChannelInitializer<>() {
            @Override
            protected void initChannel(Channel ch) {
                ch.pipeline()
                    .addLast(HandlerNames.SPLITTER, new Varint21FrameDecoder())
                    .addLast(HandlerNames.DECODER, new PacketDecoder(MinecraftVersion.V26_1, Protocol.HANDSHAKE.serverbound))
                    .addLast(HandlerNames.PREPENDER, new VarInt21FrameEncoder())
                    .addLast(HandlerNames.ENCODER, new PacketEncoder(MinecraftVersion.V26_1, Protocol.HANDSHAKE.clientbound));
                ConnectionHandle handle = new ConnectionHandle(ch, false);
                ch.pipeline().addAfter(HandlerNames.DECODER, HandlerNames.PACKET_HANDLER, new PacketHandler(new PlayerHandshakePacketListener(handle), handle));
            }
        }).bind(PORT).syncUninterruptibly();
    }
}
