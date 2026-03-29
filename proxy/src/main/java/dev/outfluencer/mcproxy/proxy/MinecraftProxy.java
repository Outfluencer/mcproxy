package dev.outfluencer.mcproxy.proxy;

import dev.outfluencer.mcproxy.config.ConfigLoader;
import dev.outfluencer.mcproxy.config.ProxyConfig;
import dev.outfluencer.mcproxy.log.ColorLogHandler;
import dev.outfluencer.mcproxy.networking.ConnectionHandle;
import dev.outfluencer.mcproxy.networking.netty.HandlerNames;
import dev.outfluencer.mcproxy.networking.netty.PacketLimiter;
import dev.outfluencer.mcproxy.networking.netty.PipelineUtil;
import dev.outfluencer.mcproxy.networking.netty.handler.InboundConnectionLimiter;
import dev.outfluencer.mcproxy.networking.netty.handler.PacketDecoder;
import dev.outfluencer.mcproxy.networking.netty.handler.PacketEncoder;
import dev.outfluencer.mcproxy.networking.netty.handler.PacketHandler;
import dev.outfluencer.mcproxy.networking.netty.handler.VarInt21FrameEncoder;
import dev.outfluencer.mcproxy.networking.netty.handler.Varint21FrameDecoder;
import dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion;
import dev.outfluencer.mcproxy.networking.protocol.registry.Protocol;
import dev.outfluencer.mcproxy.proxy.connection.handler.PlayerHandshakePacketListener;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.util.ResourceLeakDetector;
import lombok.Getter;
import lombok.SneakyThrows;

import java.nio.file.Path;
import java.util.logging.Logger;

public final class MinecraftProxy {

    static {
        ColorLogHandler.install();
    }
    @Getter
    private static final Logger logger = Logger.getLogger(MinecraftProxy.class.getName());
    @Getter
    private static final MinecraftProxy instance = new MinecraftProxy();
    @Getter
    private final ProxyConfig config;
    private final EventLoopGroup bossGroup = PipelineUtil.newEventLoopGroup(1);
    private final EventLoopGroup workerGroup = PipelineUtil.newEventLoopGroup(0);
    private final Channel serverChannl;


    @SneakyThrows
    private MinecraftProxy() {
        config = ConfigLoader.load(Path.of("config.json"), ProxyConfig.class).check();
        logger.info("Loaded configuration from config.json");

        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);
        if (System.getProperty("io.netty.allocator.type") == null) {
            System.setProperty("io.netty.allocator.type", "pooled");
        }

        serverChannl = new ServerBootstrap()
            .group(bossGroup, workerGroup)
            .channel(PipelineUtil.serverChannelType())
            .handler(new InboundConnectionLimiter(config.getConnectionThrottleLimit(), config.getConnectionThrottleMillis()))
            .childHandler(new ChannelInitializer<>() {
                @Override
                protected void initChannel(Channel ch) {
                    ch.pipeline().addLast(HandlerNames.SPLITTER, new Varint21FrameDecoder()).addLast(HandlerNames.READ_TIMEOUT, new ReadTimeoutHandler(config.getReadTimeout())).addLast(HandlerNames.DECODER, new PacketDecoder(MinecraftVersion.V26_1, Protocol.HANDSHAKE.serverbound)).addLast(HandlerNames.WRITE_TIMEOUT, new WriteTimeoutHandler(config.getWriteTimeout())).addLast(HandlerNames.PREPENDER, new VarInt21FrameEncoder()).addLast(HandlerNames.ENCODER, new PacketEncoder(MinecraftVersion.V26_1, Protocol.HANDSHAKE.clientbound));
                    ConnectionHandle handle = new ConnectionHandle(ch, false);
                    PacketHandler handler = new PacketHandler(new PlayerHandshakePacketListener(handle), handle);
                    handler.setPacketLimiter(new PacketLimiter(1 << 12, 1 << 25));
                    ch.pipeline().addLast(HandlerNames.PACKET_HANDLER, handler);
                }
            }).bind(config.getBind(), config.getPort()).syncUninterruptibly().channel();

        logger.info("Listening on " + config.getBind() + ":" + config.getPort());
    }

    public String getName() {
        return "mcproxy";
    }

    public String getVersion() {
        return "0.1";
    }
}
