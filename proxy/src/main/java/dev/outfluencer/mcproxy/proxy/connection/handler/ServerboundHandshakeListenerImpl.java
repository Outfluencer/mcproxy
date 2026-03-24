package dev.outfluencer.mcproxy.proxy.connection.handler;

import dev.outfluencer.mcproxy.networking.ConnectionHandle;
import dev.outfluencer.mcproxy.networking.protocol.DecodedPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.handshake.ServerboundHandshakePacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.handshake.ServerboundHandshakePacketListener;
import dev.outfluencer.mcproxy.networking.protocol.registry.Protocol;
import dev.outfluencer.mcproxy.proxy.connection.handler.login.ServerboundLoginPacketListenerImpl;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ServerboundHandshakeListenerImpl implements ServerboundHandshakePacketListener {

    private final ConnectionHandle handle;

    @Override
    public boolean handle(ServerboundHandshakePacket packet) {
        handle.setProtocolVersion(packet.getVersion());
        if (packet.getClientIntent().isLogin()) {
            handle.setProtocol(Protocol.LOGIN);
            handle.setPacketListener(new ServerboundLoginPacketListenerImpl(handle));
        } else {
            handle.setProtocol(Protocol.STATUS);
            handle.setPacketListener(new ServerboundStatusPacketListenerImpl(handle));
        }
        return false;
    }

    @Override
    public void handle(DecodedPacket decodedPacket) {
        throw new IllegalStateException("Unexpected DecodedPacket");
    }
}
