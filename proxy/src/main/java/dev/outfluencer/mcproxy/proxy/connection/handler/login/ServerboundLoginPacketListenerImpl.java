package dev.outfluencer.mcproxy.proxy.connection.handler.login;

import dev.outfluencer.mcproxy.networking.ConnectionHandle;
import dev.outfluencer.mcproxy.networking.protocol.DecodedPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.login.ServerboundHelloPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.login.ServerboundLoginAcknowledgedPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.login.ServerboundLoginPacketListener;
import dev.outfluencer.mcproxy.networking.protocol.registry.Protocol;
import dev.outfluencer.mcproxy.proxy.connection.BackendConnector;
import dev.outfluencer.mcproxy.proxy.connection.PlayerImpl;
import dev.outfluencer.mcproxy.proxy.connection.handler.config.ServerboundConfigurationPacketListenerImpl;
import lombok.RequiredArgsConstructor;

import java.net.InetSocketAddress;

@RequiredArgsConstructor
public class ServerboundLoginPacketListenerImpl implements ServerboundLoginPacketListener {

    private final ConnectionHandle handle;
    private PlayerImpl player;

    @Override
    public boolean handle(ServerboundHelloPacket packet) {
        player = new PlayerImpl(handle, packet.getName(), packet.getUuid());
        player.connect(new InetSocketAddress("127.0.0.1", 25566));
        return false;
    }

    @Override
    public boolean handle(ServerboundLoginAcknowledgedPacket packet) {
        handle.setProtocol(Protocol.CONFIG);
        handle.setPacketListener(new ServerboundConfigurationPacketListenerImpl(player));

        player.getServer().getConnection().sendPacket(packet);
        player.getServer().getConnection().setEncoderProtocol(Protocol.CONFIG);
        return false;
    }

    @Override
    public void handle(DecodedPacket decodedPacket) {
        throw new IllegalStateException("Unexpected DecodedPacket");
    }


}
