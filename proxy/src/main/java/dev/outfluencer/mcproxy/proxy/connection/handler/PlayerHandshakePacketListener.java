package dev.outfluencer.mcproxy.proxy.connection.handler;

import dev.outfluencer.mcproxy.api.ProxyServer;
import dev.outfluencer.mcproxy.api.events.PlayerHandshakeEvent;
import dev.outfluencer.mcproxy.networking.ConnectionHandle;
import dev.outfluencer.mcproxy.networking.protocol.packets.handshake.ServerboundHandshakePacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.handshake.ServerboundHandshakePacketListener;
import dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion;
import dev.outfluencer.mcproxy.networking.protocol.registry.Protocol;
import dev.outfluencer.mcproxy.proxy.MinecraftProxy;
import dev.outfluencer.mcproxy.proxy.connection.handler.login.PlayerLoginPacketListener;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PlayerHandshakePacketListener implements ServerboundHandshakePacketListener {

    private final ConnectionHandle handle;
    private final MinecraftProxy proxy = MinecraftProxy.getInstance();

    @Override
    public boolean handle(ServerboundHandshakePacket packet) {
        handle.setProtocolVersion(packet.getVersion());
        if (packet.getClientIntent().isLogin()) {
            handle.setProtocol(Protocol.LOGIN);
            if (!MinecraftVersion.isSupported(packet.getVersion())) {
                handle.disconnect("Unsupported version");
                return false;
            }
            handle.setPacketListener(new PlayerLoginPacketListener(handle, packet));
        } else {
            handle.setProtocol(Protocol.STATUS);
            handle.setPacketListener(new PlayerStatusPacketListener(handle));
        }

        var event = proxy.getEventManager().fire(new PlayerHandshakeEvent(handle.getAddress(), packet));
        if (event.isCancelled()){
            handle.disconnect(event.getDisconnectMessage());
        }
        return false;
    }

    @Override
    public String toString() {
        return "[" + getClass().getSimpleName() + "|" + handle.getAddress() + "]";
    }

}
