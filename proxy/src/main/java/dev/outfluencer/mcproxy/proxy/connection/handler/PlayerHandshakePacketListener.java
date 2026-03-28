package dev.outfluencer.mcproxy.proxy.connection.handler;

import dev.outfluencer.mcproxy.config.ProxyConfig;
import dev.outfluencer.mcproxy.networking.ConnectionHandle;
import dev.outfluencer.mcproxy.networking.netty.QuietException;
import dev.outfluencer.mcproxy.networking.protocol.DecodedPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.handshake.ServerboundHandshakePacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.handshake.ServerboundHandshakePacketListener;
import dev.outfluencer.mcproxy.networking.protocol.registry.Protocol;
import dev.outfluencer.mcproxy.proxy.connection.handler.login.PlayerLoginPacketListener;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PlayerHandshakePacketListener implements ServerboundHandshakePacketListener {

    private final ConnectionHandle handle;
    private final ProxyConfig config;

    @Override
    public boolean handle(ServerboundHandshakePacket packet) {
        handle.setProtocolVersion(packet.getVersion());
        if (packet.getClientIntent().isLogin()) {
            handle.setProtocol(Protocol.LOGIN);
            handle.setPacketListener(new PlayerLoginPacketListener(handle, config));
        } else {
            handle.setProtocol(Protocol.STATUS);
            handle.setPacketListener(new PlayerStatusPacketListener(handle, config));
        }
        return false;
    }

    @Override
    public String toString() {
        return "[" + getClass().getSimpleName() + "|" + handle.getAddress() + "]";
    }

}
