package dev.outfluencer.mcproxy.proxy.connection.handler.login;

import dev.outfluencer.mcproxy.networking.ConnectionHandle;
import dev.outfluencer.mcproxy.networking.protocol.DecodedPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.game.ClientboundBundleDelimiterPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.game.ClientboundStartConfigurationPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.handshake.ServerboundHandshakePacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.login.ClientboundLoginCompressionPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.login.ClientboundLoginDisconnectPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.login.ClientboundLoginFinishedPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.login.ClientboundLoginPacketListener;
import dev.outfluencer.mcproxy.networking.protocol.packets.login.ServerboundHelloPacket;
import dev.outfluencer.mcproxy.networking.protocol.registry.Protocol;
import dev.outfluencer.mcproxy.proxy.connection.PlayerImpl;
import dev.outfluencer.mcproxy.proxy.connection.ServerImpl;
import dev.outfluencer.mcproxy.proxy.connection.handler.config.ClientboundConfigurationPacketListenerImpl;
import lombok.RequiredArgsConstructor;

import java.net.InetSocketAddress;

@RequiredArgsConstructor
public class ClientboundLoginPacketListenerImpl implements ClientboundLoginPacketListener {

    private final ConnectionHandle backendHandle;
    private final PlayerImpl player;
    private final InetSocketAddress backendAddress;

    @Override
    public void onConnect() {
        ServerboundHandshakePacket handshake = new ServerboundHandshakePacket(player.getConnection().getProtocolVersion(), backendAddress.getHostString(), backendAddress.getPort(), ServerboundHandshakePacket.ClientIntent.LOGIN);
        backendHandle.sendPacket(handshake);
        backendHandle.setProtocol(Protocol.LOGIN);
        backendHandle.sendPacket(new ServerboundHelloPacket(player.getName(), player.getUuid()));
    }

    @Override
    public boolean handle(ClientboundLoginDisconnectPacket packet) {
        backendHandle.close(null);
        return false;
    }

    @Override
    public boolean handle(ClientboundLoginFinishedPacket packet) {
        ServerImpl server = player.getServer();
        if (server != null) {
            if (server.isConnected()) {
                server.disconnect();
                server.getConnection()
                    .getChannel()
                    .closeFuture()
                    .addListener(_ -> updatePlayerServer(packet));
                return false;
            }
        }
        updatePlayerServer(packet);
        return false;
    }

    @Override
    public boolean handle(ClientboundLoginCompressionPacket clientboundLoginCompressionPacket) {
        return false;
    }

    public void updatePlayerServer(ClientboundLoginFinishedPacket packet) {
        if (!player.isConnected()) {
            return;
        }

        ServerImpl server = new ServerImpl(player, backendHandle);
        player.setServer(server);
        server.setDecoderProtocol(Protocol.CONFIG);
        backendHandle.setPacketListener(new ClientboundConfigurationPacketListenerImpl(server));

        Protocol protocol = player.getEncoderProtocol();
        if (protocol == Protocol.LOGIN) {
            player.sendPacket(packet);
        } else if (protocol == Protocol.GAME) {

            if (player.isBundling()) {
                player.toggleBundle();
                player.sendPacket(new ClientboundBundleDelimiterPacket());
            }

            player.sendPacket(new ClientboundStartConfigurationPacket());
            player.setEncoderProtocol(Protocol.CONFIG);
        } else {
            throw new UnsupportedOperationException();
        }

    }

    @Override
    public void onException(Throwable throwable) {
        backendHandle.close(null);
    }


    @Override
    public void handle(DecodedPacket decodedPacket) {
        throw new IllegalStateException("Unexpected DecodedPacket");
    }
}
