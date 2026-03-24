package dev.outfluencer.mcproxy.proxy.connection.handler.login;

import dev.outfluencer.mcproxy.networking.ConnectionHandle;
import dev.outfluencer.mcproxy.networking.protocol.DecodedPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.game.ClientboundBundleDelimiterPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.game.ClientboundStartConfigurationPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.handshake.ServerboundHandshakePacket;
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
        backendHandle.secureClose(null);
        return false;
    }

    @Override
    public boolean handle(ClientboundLoginFinishedPacket packet) {
        if (player.getServer() != null && !player.getServer().getConnection().isClosed()) {
            player.getServer().getConnection().secureCloseAndThen(null, () -> updatePlayerServer(packet));
        } else {
            updatePlayerServer(packet);
        }
        return false;
    }

    public void updatePlayerServer(ClientboundLoginFinishedPacket packet) {
        ServerImpl server = new ServerImpl(player, backendHandle);
        backendHandle.setDecoderProtocol(Protocol.CONFIG);
        backendHandle.setPacketListener(new ClientboundConfigurationPacketListenerImpl(server));
        player.setServer(server);

        Protocol protocol = player.getConnection().getEncoderProtocol();
        if (protocol == Protocol.LOGIN) {
            player.getConnection().sendPacket(packet);
        } else if (protocol == Protocol.GAME) {

            if (player.isBundling()) {
                player.toggleBundle();
                player.getConnection().sendPacket(new ClientboundBundleDelimiterPacket());
            }

            player.getConnection().sendPacket(new ClientboundStartConfigurationPacket());
            player.getConnection().setEncoderProtocol(Protocol.CONFIG);
        } else {
            throw new UnsupportedOperationException();
        }

    }

    @Override
    public void onException(Throwable throwable) {
        backendHandle.secureClose(null);
    }


    @Override
    public void handle(DecodedPacket decodedPacket) {
        throw new IllegalStateException("Unexpected DecodedPacket");
    }
}
