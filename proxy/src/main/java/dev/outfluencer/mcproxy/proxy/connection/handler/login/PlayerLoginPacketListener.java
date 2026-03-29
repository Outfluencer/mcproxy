package dev.outfluencer.mcproxy.proxy.connection.handler.login;

import dev.outfluencer.mcproxy.networking.ConnectionHandle;
import dev.outfluencer.mcproxy.networking.netty.QuietException;
import dev.outfluencer.mcproxy.networking.protocol.packets.login.ClientboundLoginCompressionPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.login.ServerboundHelloPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.login.ServerboundLoginAcknowledgedPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.login.ServerboundLoginPacketListener;
import dev.outfluencer.mcproxy.proxy.MinecraftProxy;
import dev.outfluencer.mcproxy.proxy.connection.PlayerImpl;
import dev.outfluencer.mcproxy.proxy.connection.handler.config.PlayerConfigurationPacketListener;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PlayerLoginPacketListener implements ServerboundLoginPacketListener {

    private enum State {
        AWAIT_HELLO, RECEIVED_HELLO, AWAIT_LOGIN_ACKNOWLEDGED, RECEIVED_LOGIN_ACKNOWLEDGED
    }
    private final MinecraftProxy proxy = MinecraftProxy.getInstance();
    private final ConnectionHandle handle;
    private State state = State.AWAIT_HELLO;
    private PlayerImpl player;

    @Override
    public boolean handle(ServerboundHelloPacket packet) {
        stateTransition(State.AWAIT_HELLO, State.RECEIVED_HELLO, "Unexpected ServerboundHelloPacket");
        player = new PlayerImpl(handle, packet.getName(), packet.getUuid());
        finishPlayerLogin();
        return false;
    }

    public void setCompression(int threshold) {
        player.sendPacket(new ClientboundLoginCompressionPacket(threshold));
        handle.setCompression(threshold);
    }

    public void finishPlayerLogin() {
        setCompression(proxy.getConfig().getCompressionThreshold());
        player.fallback();
        state = State.AWAIT_LOGIN_ACKNOWLEDGED;
    }

    @Override
    public boolean handle(ServerboundLoginAcknowledgedPacket packet) {
        stateTransition(State.AWAIT_LOGIN_ACKNOWLEDGED, State.RECEIVED_LOGIN_ACKNOWLEDGED, "Unexpected ServerboundLoginAcknowledgedPacket");
        handle.setPacketListener(new PlayerConfigurationPacketListener(player));
        return true;
    }

    private void stateTransition(State expected, State next, String errorMessage) {
        if (state == expected) {
            state = next;
        } else {
            throw new QuietException(errorMessage);
        }
    }

    @Override
    public void onDisconnect() {
        if (player != null) {
            player.disconnectPendingConnections();
        }
    }

    @Override
    public String toString() {
        String name = player != null ? player.getName() : null;
        return "[" + getClass().getSimpleName() + "|" + (name != null ? name + "|" : "") + handle.getAddress() + "]";
    }
}
