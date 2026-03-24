package dev.outfluencer.mcproxy.proxy;

import dev.outfluencer.mcproxy.networking.ConnectionHandle;
import dev.outfluencer.mcproxy.networking.netty.PacketDecoder;
import dev.outfluencer.mcproxy.networking.netty.PacketEncoder;
import dev.outfluencer.mcproxy.networking.netty.PacketHandler;
import dev.outfluencer.mcproxy.networking.netty.PipelineUtil;
import dev.outfluencer.mcproxy.networking.netty.Varint21FrameDecoder;
import dev.outfluencer.mcproxy.networking.netty.VarInt21FrameEncoder;
import dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion;
import dev.outfluencer.mcproxy.networking.protocol.registry.Protocol;
import dev.outfluencer.mcproxy.proxy.connection.handler.ServerboundHandshakeListenerImpl;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBufAllocator;
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
                ConnectionHandle handle = new ConnectionHandle(ch, false);
                ch.pipeline()
                    .addLast("frame-decoder", new Varint21FrameDecoder())
                    .addLast("packet-decoder", new PacketDecoder(MinecraftVersion.V26_1, Protocol.HANDSHAKE.serverbound))
                    .addLast("handler", new PacketHandler(new ServerboundHandshakeListenerImpl(handle), handle))
                    .addLast("frame-prepender", new VarInt21FrameEncoder())
                    .addLast("packet-encoder", new PacketEncoder(MinecraftVersion.V26_1, Protocol.HANDSHAKE.clientbound));
            }
        }).bind(PORT).syncUninterruptibly();
    }
}
