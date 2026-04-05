package dev.outfluencer.mcproxy.proxy;

import dev.outfluencer.mcproxy.api.ProxyServer;
import dev.outfluencer.mcproxy.api.connection.Player;
import dev.outfluencer.mcproxy.api.events.unsafe.ChannelInitializedEvent;
import dev.outfluencer.mcproxy.config.ConfigLoader;
import dev.outfluencer.mcproxy.event.EventManager;
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
import dev.outfluencer.mcproxy.proxy.config.ProxyConfig;
import dev.outfluencer.mcproxy.proxy.connection.PlayerImpl;
import dev.outfluencer.mcproxy.proxy.connection.handler.PlayerHandshakePacketListener;
import dev.outfluencer.mcproxy.proxy.plugin.PluginLoader;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.util.ResourceLeakDetector;
import lombok.Getter;
import lombok.Locked;
import lombok.SneakyThrows;
import lombok.Synchronized;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

public final class MinecraftProxy extends ProxyServer {

    static {
        ColorLogHandler.install();
    }

    @Getter
    private static final Logger logger = Logger.getLogger(MinecraftProxy.class.getName());
    @Getter
    private static final MinecraftProxy instance = new MinecraftProxy();
    @Getter
    private final ProxyConfig config;
    @Getter
    private final PluginLoader pluginLoader;
    @Getter
    private final EventManager eventManager;
    private final EventLoopGroup bossGroup = PipelineUtil.newEventLoopGroup(1);
    private final EventLoopGroup workerGroup = PipelineUtil.newEventLoopGroup(0);
    private final Channel serverChannl;

    private final ReentrantReadWriteLock playerLock = new ReentrantReadWriteLock();
    private final Map<String, PlayerImpl> playersByName = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private final Map<UUID, PlayerImpl> playersByUuid = new HashMap<>();
    private final Object shutdownLock = new Object();
    private boolean shuttingDown = false;


    @Override
    @Locked.Read("playerLock")
    public Collection<Player> getPlayers() {
        return Collections.unmodifiableCollection(playersByUuid.values());
    }

    @Override
    @Locked.Read("playerLock")
    public int getOnlinePlayerCount() {
        return playersByUuid.size();
    }

    @Override
    @Locked.Read("playerLock")
    public PlayerImpl getPlayer(UUID uuid) {
        return playersByUuid.get(uuid);
    }

    @Override
    @Locked.Read("playerLock")
    public PlayerImpl getPlayer(String name) {
        return playersByName.get(name);
    }

    @Locked.Write("playerLock")
    public boolean addPlayer(PlayerImpl player) {
        if (playersByName.containsKey(player.getName()) || playersByUuid.containsKey(player.getUuid())) {
            return false;
        }
        playersByName.put(player.getName(), player);
        playersByUuid.put(player.getUuid(), player);
        player.getConnection().getChannel().closeFuture().addListener(_ -> removePlayer(player));
        return true;
    }

    @Locked.Write("playerLock")
    public void removePlayer(PlayerImpl player) {
        playersByName.remove(player.getName());
        playersByUuid.remove(player.getUuid());
    }


    @SneakyThrows
    private MinecraftProxy() {
        ProxyServer.setInstance(this);
        eventManager = new EventManager();
        config = ConfigLoader.load(Path.of("config.json"), ProxyConfig.class).check();
        logger.info("Loaded configuration from config.json");

        pluginLoader = new PluginLoader(Path.of("plugins"));
        pluginLoader.loadPlugins();

        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);
        if (System.getProperty("io.netty.allocator.type") == null) {
            System.setProperty("io.netty.allocator.type", "pooled");
        }

        serverChannl = new ServerBootstrap().group(bossGroup, workerGroup).channel(PipelineUtil.serverChannelType()).handler(new InboundConnectionLimiter(config.getConnectionThrottleLimit(), config.getConnectionThrottleMillis())).childHandler(new ChannelInitializer<>() {
            @Override
            protected void initChannel(Channel ch) {
                ch.pipeline().addLast(HandlerNames.SPLITTER, new Varint21FrameDecoder()).addLast(HandlerNames.READ_TIMEOUT, new ReadTimeoutHandler(config.getReadTimeout())).addLast(HandlerNames.DECODER, new PacketDecoder(MinecraftVersion.V26_1, Protocol.HANDSHAKE.serverbound)).addLast(HandlerNames.WRITE_TIMEOUT, new WriteTimeoutHandler(config.getWriteTimeout())).addLast(HandlerNames.PREPENDER, new VarInt21FrameEncoder()).addLast(HandlerNames.ENCODER, new PacketEncoder(MinecraftVersion.V26_1, Protocol.HANDSHAKE.clientbound));
                ConnectionHandle handle = new ConnectionHandle(ch, false);
                PacketHandler handler = new PacketHandler(new PlayerHandshakePacketListener(handle), handle);
                handler.setPacketLimiter(new PacketLimiter(1 << 12, 1 << 25));
                ch.pipeline().addLast(HandlerNames.PACKET_HANDLER, handler);
                eventManager.fire(new ChannelInitializedEvent(ch, ChannelInitializedEvent.Type.FRONTEND));
            }
        }).bind(config.getBind(), config.getPort()).syncUninterruptibly().channel();
        logger.info("Listening on " + config.getBind() + ":" + config.getPort());

        pluginLoader.enablePlugins();
    }

    @Synchronized("shutdownLock")
    public void stop() {
        if(shuttingDown) {
            return;
        }
        shuttingDown = true;
        getPlayers().forEach(player -> player.disconnect("Proxy shutdown"));
        pluginLoader.disablePlugins();
        bossGroup.close();
        workerGroup.close();
        serverChannl.close().syncUninterruptibly();
    }

    public String getName() {
        return "mcproxy";
    }

    public String getVersion() {
        return "0.1";
    }
}
