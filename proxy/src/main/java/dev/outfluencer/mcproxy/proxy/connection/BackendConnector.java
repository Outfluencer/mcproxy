package dev.outfluencer.mcproxy.proxy.connection;

import dev.outfluencer.mcproxy.api.ProxyServer;
import dev.outfluencer.mcproxy.api.events.unsafe.ChannelInitializedEvent;
import dev.outfluencer.mcproxy.api.ServerInfo;
import dev.outfluencer.mcproxy.networking.ConnectionHandle;
import dev.outfluencer.mcproxy.networking.netty.HandlerNames;
import dev.outfluencer.mcproxy.networking.netty.PipelineUtil;
import dev.outfluencer.mcproxy.networking.netty.handler.PacketDecoder;
import dev.outfluencer.mcproxy.networking.netty.handler.PacketEncoder;
import dev.outfluencer.mcproxy.networking.netty.handler.PacketHandler;
import dev.outfluencer.mcproxy.networking.netty.handler.VarInt21FrameEncoder;
import dev.outfluencer.mcproxy.networking.netty.handler.Varint21FrameDecoder;
import dev.outfluencer.mcproxy.networking.protocol.registry.Protocol;
import dev.outfluencer.mcproxy.proxy.MinecraftProxy;
import dev.outfluencer.mcproxy.proxy.connection.handler.login.ServerLoginPacketListener;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BackendConnector {

    private final PlayerImpl player;
    private final ServerInfo serverInfo;

    public void connect() {
        int protocolVersion = player.getConnection().getProtocolVersion();
        int readTimeout = MinecraftProxy.getInstance().getConfig().getReadTimeout();
        int writeTimeout = MinecraftProxy.getInstance().getConfig().getWriteTimeout();

        new Bootstrap().group(player.getConnection().getChannel().eventLoop()).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000).channel(PipelineUtil.clientChannelType()).handler(new ChannelInitializer<>() {
            @Override
            protected void initChannel(Channel ch) {
                ch.pipeline()
                    .addLast(HandlerNames.SPLITTER, new Varint21FrameDecoder())
                    .addLast(HandlerNames.READ_TIMEOUT, new ReadTimeoutHandler(readTimeout))
                    .addLast(HandlerNames.DECODER, new PacketDecoder(0, Protocol.HANDSHAKE.clientbound))
                    .addLast(HandlerNames.WRITE_TIMEOUT, new WriteTimeoutHandler(writeTimeout))
                    .addLast(HandlerNames.PREPENDER, new VarInt21FrameEncoder())
                    .addLast(HandlerNames.ENCODER, new PacketEncoder(0, Protocol.HANDSHAKE.serverbound));

                ConnectionHandle backendHandle = new ConnectionHandle(ch, true);
                backendHandle.setProtocolVersion(protocolVersion);

                ServerImpl server = new ServerImpl(serverInfo, player, backendHandle);
                player.addPendingConnection(server);
                ch.pipeline().addAfter(HandlerNames.DECODER, HandlerNames.PACKET_HANDLER, new PacketHandler(new ServerLoginPacketListener(server), backendHandle));
                ProxyServer.getInstance().getEventManager().fire(new ChannelInitializedEvent(ch, ChannelInitializedEvent.Type.BACKEND));
            }
        }).connect(serverInfo.getSocketAddress()).addListener((ChannelFutureListener) channelFuture -> {
            if (!channelFuture.isSuccess()) {
                if (!player.isConnectedToServer()) {
                    player.connectToNextFallback();
                }
            }
        });
    }
}
