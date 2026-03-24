package dev.outfluencer.mcproxy.proxy.connection;

import dev.outfluencer.mcproxy.networking.ConnectionHandle;
import dev.outfluencer.mcproxy.networking.netty.PacketDecoder;
import dev.outfluencer.mcproxy.networking.netty.PacketEncoder;
import dev.outfluencer.mcproxy.networking.netty.PacketHandler;
import dev.outfluencer.mcproxy.networking.netty.PipelineUtil;
import dev.outfluencer.mcproxy.networking.netty.VarInt21FrameEncoder;
import dev.outfluencer.mcproxy.networking.netty.Varint21FrameDecoder;
import dev.outfluencer.mcproxy.networking.protocol.registry.Protocol;
import dev.outfluencer.mcproxy.proxy.connection.handler.login.ClientboundLoginPacketListenerImpl;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import lombok.RequiredArgsConstructor;

import java.net.InetSocketAddress;

@RequiredArgsConstructor
public class BackendConnector {

    private final PlayerImpl player;
    private final InetSocketAddress address;

    public void connect() {
        int protocolVersion = player.getConnection().getProtocolVersion();

        new Bootstrap()
            .group(player.getConnection().getChannel().eventLoop())
            .channel(PipelineUtil.clientChannelType())
            .handler(new ChannelInitializer<>() {
                @Override
                protected void initChannel(Channel ch) {
                    ConnectionHandle backendHandle = new ConnectionHandle(ch, true);
                    ch.pipeline()
                        .addLast("frame-decoder", new Varint21FrameDecoder())
                        .addLast("packet-decoder", new PacketDecoder(protocolVersion, Protocol.HANDSHAKE.clientbound))
                        .addLast("handler", new PacketHandler(new ClientboundLoginPacketListenerImpl(backendHandle, player, address),backendHandle))
                        .addLast("frame-prepender", new VarInt21FrameEncoder())
                        .addLast("packet-encoder", new PacketEncoder(protocolVersion, Protocol.HANDSHAKE.serverbound));
                }
            })
            .connect(address);
    }
}
