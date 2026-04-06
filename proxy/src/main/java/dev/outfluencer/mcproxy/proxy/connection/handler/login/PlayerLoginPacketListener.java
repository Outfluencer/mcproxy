package dev.outfluencer.mcproxy.proxy.connection.handler.login;

import com.google.gson.Gson;
import dev.outfluencer.mcproxy.api.events.CompressionChangeEvent;
import dev.outfluencer.mcproxy.api.events.PlayerAuthenticateEvent;
import dev.outfluencer.mcproxy.api.events.PlayerLoggedInEvent;
import dev.outfluencer.mcproxy.api.events.PlayerLoginEvent;
import dev.outfluencer.mcproxy.networking.ConnectionHandle;
import dev.outfluencer.mcproxy.networking.netty.QuietException;
import dev.outfluencer.mcproxy.networking.protocol.packets.handshake.ServerboundHandshakePacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.login.ClientboundLoginCompressionPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.login.ClientboundLoginEncryptionRequestPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.login.ServerboundHelloPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.login.ServerboundLoginAcknowledgedPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.login.ServerboundLoginEncryptionResponsePacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.login.ServerboundLoginPacketListener;
import dev.outfluencer.mcproxy.proxy.MinecraftProxy;
import dev.outfluencer.mcproxy.proxy.config.ProxyConfig;
import dev.outfluencer.mcproxy.proxy.connection.CryptUtil;
import dev.outfluencer.mcproxy.proxy.connection.LoginResult;
import dev.outfluencer.mcproxy.proxy.connection.PlayerImpl;
import dev.outfluencer.mcproxy.proxy.connection.handler.config.PlayerConfigurationPacketListener;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;
import java.util.concurrent.Executor;

@RequiredArgsConstructor
public class PlayerLoginPacketListener implements ServerboundLoginPacketListener {
    private static final Gson gson = new Gson();

    private enum State {
        AWAIT_HELLO, RECEIVED_HELLO, AWAIT_LOGIN_ACKNOWLEDGED, RECEIVED_LOGIN_ACKNOWLEDGED, AWAIT_ENCRYPTION_RESPONSE, RECEIVED_ENCRYPTION_RESPONSE
    }

    private final MinecraftProxy proxy = MinecraftProxy.getInstance();
    private final ProxyConfig.OnlineMode online = proxy.getConfig().getOnline();
    private final ConnectionHandle handle;
    private final ServerboundHandshakePacket handshake;
    private State state = State.AWAIT_HELLO;
    private PlayerImpl player;
    private ClientboundLoginEncryptionRequestPacket encryptionRequest;

    @Override
    public boolean handle(ServerboundHelloPacket packet) {
        stateTransition(State.AWAIT_HELLO, State.RECEIVED_HELLO, "Unexpected ServerboundHelloPacket");
        player = new PlayerImpl(handle, packet.getName(), handshake);
        proxy.getEventManager().fireAsync(new PlayerLoginEvent(player), handle.eventCallback(event -> {
            if (event.isCancelled()) {
                player.disconnect(event.getDisconnectMessage());
                return;
            }
            if (online.encrypt()) {
                state = State.AWAIT_ENCRYPTION_RESPONSE;
                player.sendPacket(encryptionRequest = CryptUtil.encryptRequest(online.auth()));
            } else {
                finishPlayerLogin();
            }
        }), handle.getChannel().eventLoop());
        return DROP;
    }

    @Override
    @SneakyThrows
    public boolean handle(ServerboundLoginEncryptionResponsePacket packet) {
        stateTransition(State.AWAIT_ENCRYPTION_RESPONSE, State.RECEIVED_ENCRYPTION_RESPONSE, "Unexpected ServerboundLoginEncryptionResponsePacket");
        CryptUtil.check(packet, encryptionRequest);
        final SecretKey secretKey = CryptUtil.getSecret(packet);
        handle.setEncryption(secretKey);
        if (online.auth()) {
            Executor executor = handle.getChannel().eventLoop();
            proxy.getEventManager().fireAsync(new PlayerAuthenticateEvent(player), handle.eventCallback(event -> {
                if(event.isCancelled()) {
                    player.disconnect(event.getDisconnectMessage());
                    return;
                }
                auth(executor, secretKey);
            }), executor);
        } else {
            finishPlayerLogin();
        }
        return DROP;
    }

    @SneakyThrows
    private void auth(Executor executor, SecretKey secretKey) {
        String encodedName = URLEncoder.encode(player.getName(), StandardCharsets.UTF_8);
        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        for (byte[] bit : new byte[][] {encryptionRequest.getServerId().getBytes("ISO_8859_1"), secretKey.getEncoded(), CryptUtil.keys.getPublic().getEncoded()}) {
            sha.update(bit);
        }
        String encodedHash = URLEncoder.encode(new BigInteger(sha.digest()).toString(16), StandardCharsets.UTF_8);
        String authURL = "https://sessionserver.mojang.com/session/minecraft/hasJoined?username=" + encodedName + "&serverId=" + encodedHash;
        try (HttpClient client = HttpClient.newHttpClient()) {
            client.sendAsync(HttpRequest.newBuilder(URI.create(authURL)).build(), HttpResponse.BodyHandlers.ofString()).thenAcceptAsync(response -> {
                LoginResult loginResult = gson.fromJson(response.body(), LoginResult.class);
                player.setName(loginResult.getName());
                player.setUuid(UUID.fromString(loginResult.getId().replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5")));
                player.setLoginResult(loginResult);
                finishPlayerLogin();
            }, executor).exceptionallyAsync(throwable -> {
                handle.fireException(throwable);
                return null;
            }, executor);
        }
    }

    public void setCompression(int threshold) {
        player.sendPacket(new ClientboundLoginCompressionPacket(threshold));
        handle.setCompression(threshold);
        proxy.getEventManager().fire(new CompressionChangeEvent(threshold, player));
    }

    public void finishPlayerLogin() {
        if (player.getUuid() == null) {
            player.setUuid(UUID.nameUUIDFromBytes(("OfflinePlayer:" + player.getName()).getBytes(StandardCharsets.UTF_8)));
        }
        if (!proxy.addPlayer(player)) {
            player.disconnect("Already connected to the proxy");
            return;
        }

        proxy.getEventManager().fireAsync(new PlayerLoggedInEvent(player), player.getConnection().eventCallback(event -> {
            if (event.isCancelled()) {
                player.disconnect(event.getDisconnectMessage());
                return;
            }

            setCompression(proxy.getConfig().getCompressionThreshold());
            player.fallback();
            state = State.AWAIT_LOGIN_ACKNOWLEDGED;
        }), player.getConnection().getChannel().eventLoop());
    }

    @Override
    public boolean handle(ServerboundLoginAcknowledgedPacket packet) {
        stateTransition(State.AWAIT_LOGIN_ACKNOWLEDGED, State.RECEIVED_LOGIN_ACKNOWLEDGED, "Unexpected ServerboundLoginAcknowledgedPacket");
        handle.setPacketListener(new PlayerConfigurationPacketListener(player));
        return PASS;
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
