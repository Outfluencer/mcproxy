package dev.outfluencer.mcproxy.proxy.connection;

import dev.outfluencer.mcproxy.networking.ConnectionHandle;
import dev.outfluencer.mcproxy.networking.netty.HandlerNames;
import dev.outfluencer.mcproxy.networking.netty.PipelineUtil;
import dev.outfluencer.mcproxy.networking.netty.handler.PacketDecoder;
import dev.outfluencer.mcproxy.networking.netty.handler.PacketEncoder;
import dev.outfluencer.mcproxy.networking.netty.handler.PacketHandler;
import dev.outfluencer.mcproxy.networking.netty.handler.VarInt21FrameEncoder;
import dev.outfluencer.mcproxy.networking.netty.handler.Varint21FrameDecoder;
import dev.outfluencer.mcproxy.networking.protocol.registry.Protocol;
import dev.outfluencer.mcproxy.proxy.connection.handler.login.ServerLoginPacketListener;
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

        new Bootstrap().group(player.getConnection().getChannel().eventLoop()).channel(PipelineUtil.clientChannelType()).handler(new ChannelInitializer<>() {
            @Override
            protected void initChannel(Channel ch) {
                ch.pipeline()
                    .addLast(HandlerNames.SPLITTER, new Varint21FrameDecoder())
                    .addLast(HandlerNames.DECODER, new PacketDecoder(protocolVersion, Protocol.HANDSHAKE.clientbound))
                    .addLast(HandlerNames.PREPENDER, new VarInt21FrameEncoder())
                    .addLast(HandlerNames.ENCODER, new PacketEncoder(protocolVersion, Protocol.HANDSHAKE.serverbound));
                ConnectionHandle backendHandle = new ConnectionHandle(ch, true);
                ch.pipeline().addAfter(HandlerNames.DECODER, HandlerNames.PACKET_HANDLER, new PacketHandler(new ServerLoginPacketListener(backendHandle, player, address), backendHandle));
            }
        }).connect(address);
    }
}
