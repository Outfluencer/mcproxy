package dev.outfluencer.mcproxy.proxy.connection.handler.login;

import dev.outfluencer.mcproxy.api.events.CompressionChangeEvent;
import dev.outfluencer.mcproxy.api.events.PlayerLoginEvent;
import dev.outfluencer.mcproxy.networking.ConnectionHandle;
import dev.outfluencer.mcproxy.networking.netty.QuietException;
import dev.outfluencer.mcproxy.networking.protocol.packets.login.*;
import dev.outfluencer.mcproxy.proxy.MinecraftProxy;
import dev.outfluencer.mcproxy.proxy.connection.CryptUtil;
import dev.outfluencer.mcproxy.proxy.connection.PlayerImpl;
import dev.outfluencer.mcproxy.proxy.connection.handler.config.PlayerConfigurationPacketListener;
import lombok.RequiredArgsConstructor;
import net.lenni0451.mcstructs.text.TextComponent;

import javax.crypto.SecretKey;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class PlayerLoginPacketListener implements ServerboundLoginPacketListener {

    private enum State {
        AWAIT_HELLO, RECEIVED_HELLO, AWAIT_LOGIN_ACKNOWLEDGED, RECEIVED_LOGIN_ACKNOWLEDGED,
        AWAIT_ENCRYPTION_RESPONSE
    }

    private final MinecraftProxy proxy = MinecraftProxy.getInstance();
    private final boolean online = proxy.getConfig().isOnline();
    private final ConnectionHandle handle;
    private State state = State.AWAIT_HELLO;
    private PlayerImpl player;
    private ClientboundLoginEncryptionRequestPacket encryptionRequest;

    @Override
    public boolean handle(ServerboundHelloPacket packet) {
        stateTransition(State.AWAIT_HELLO, State.RECEIVED_HELLO, "Unexpected ServerboundHelloPacket");
        player = new PlayerImpl(handle, packet.getName(), packet.getUuid());
        proxy.getEventManager().fireAsync(new PlayerLoginEvent(player), handle.eventCallback(event -> {
            if(event.isCancelled()) {
                player.disconnect(event.getDisconnectMessage());
                return;
            }
            if (online) {
                state = State.AWAIT_ENCRYPTION_RESPONSE;
                player.sendPacket(encryptionRequest = CryptUtil.encryptRequest());
            } else {
                finishPlayerLogin();
            }
        }), handle.getChannel().eventLoop());
        return false;
    }

    @Override
    public boolean handle(ServerboundLoginEncryptionResponsePacket packet) {
        CryptUtil.check(packet,encryptionRequest);
        SecretKey secretKey = CryptUtil.getSecret(packet);
        handle.setEncryption(secretKey);
        player.disconnect("NÖÖÖÖ");
        return false;
    }

    public void setCompression(int threshold) {
        player.sendPacket(new ClientboundLoginCompressionPacket(threshold));
        handle.setCompression(threshold);
        proxy.getEventManager().fire(new CompressionChangeEvent(threshold, player));
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
