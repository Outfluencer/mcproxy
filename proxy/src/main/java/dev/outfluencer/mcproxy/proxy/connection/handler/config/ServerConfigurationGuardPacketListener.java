package dev.outfluencer.mcproxy.proxy.connection.handler.config;

import dev.outfluencer.mcproxy.networking.protocol.DecodedPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.config.ClientboundFinishConfigurationPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.config.ClientboundSelectKnownPacks;
import dev.outfluencer.mcproxy.networking.protocol.packets.config.ServerboundFinishConfigurationPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.game.ServerboundConfigurationAcknowledgedPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.login.ClientboundLoginFinishedPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.login.ServerboundLoginAcknowledgedPacket;
import dev.outfluencer.mcproxy.networking.protocol.registry.Protocol;
import dev.outfluencer.mcproxy.proxy.connection.ServerImpl;
import dev.outfluencer.mcproxy.proxy.connection.handler.game.ServerGamePacketListener;

import java.util.Objects;

public class ServerConfigurationGuardPacketListener extends ServerConfigurationPacketListener {

    // Server's SelectKnownPacks, stashed when we answer from cache without
    // telling the client. The client only constructs its KnownPacksManager
    // inside handleSelectKnownPacks, so if the rest of the configuration turns
    // out to differ we must replay this to the client before any registry data
    // — otherwise every tag=null entry fails with "Failed to parse local data"
    private ClientboundSelectKnownPacks pendingKnownPacks;

    public ServerConfigurationGuardPacketListener(ServerImpl server) {
        super(server);
    }

    @Override
    public void transferPlayer(ClientboundLoginFinishedPacket packet) {
        player.setServer(server);
        Protocol playerEncoderProtocol = player.getEncoderProtocol();
        if (playerEncoderProtocol == Protocol.LOGIN) {
            player.sendPacket(packet);
        } else if (playerEncoderProtocol == Protocol.CONFIG) {
            server.sendPacket(new ServerboundLoginAcknowledgedPacket());
        } else if (playerEncoderProtocol == Protocol.GAME) {
            server.sendPacket(server.getEncoderProtocol() == Protocol.LOGIN ? new ServerboundLoginAcknowledgedPacket() : new ServerboundConfigurationAcknowledgedPacket());
        }
    }

    @Override
    public void handle(DecodedPacket decodedPacket) {
        if (player.getEncoderProtocol() == Protocol.CONFIG) {
            super.handle(decodedPacket);
        }
    }

    @Override
    public boolean handle(ClientboundFinishConfigurationPacket packet) {
        if (player.getEncoderProtocol() == Protocol.CONFIG) {
            return super.handle(packet);
        }
        // Fast path: new server's configuration matches what the player already
        // has. Nothing to change on the client side — finish on the server side
        // and leave the client in GAME.
        if (Objects.deepEquals(registryAccumulationQueue.toArray(), player.getLastRegistryData())) {
            server.getConnection().setPacketListener(new ServerGamePacketListener(server));
            server.sendPacket(new ServerboundFinishConfigurationPacket());
            if (player.getSettings() != null) {
                server.sendPacket(player.getSettings());
            }
            pendingKnownPacks = null;
            return DROP;
        }
        // Slow path: configuration actually differs. Move the client into
        // CONFIG; if we earlier answered the server's SelectKnownPacks from
        // cache without informing the client, replay that packet now so the
        // client's KnownPacksManager is initialized before the registry data.
        switchToConfigAndThen(() -> {
            if (pendingKnownPacks != null) {
                player.sendPacket(pendingKnownPacks);
                pendingKnownPacks = null;
            }
            handle(packet);
        });
        return DROP;
    }

    @Override
    public boolean handle(ClientboundSelectKnownPacks packet) {
        if (player.getEncoderProtocol() == Protocol.CONFIG) {
            return super.handle(packet);
        }
        // Client's known-packs response is stable between sessions, so if the
        // server is asking about the same packs we can answer from cache and
        // avoid disturbing the client. Stash the server's packet — if the
        // registry data that follows turns out to differ from what the client
        // already has, we'll need to replay this to the client before flushing.
        if (player.getLastClientKnownPacks() != null && packet.equals(player.getLastServerKnownPacks())) {
            pendingKnownPacks = packet;
            server.sendPacket(player.getLastClientKnownPacks());
        } else {
            player.setLastServerKnownPacks(packet);
            switchToConfigAndThen(() -> handle(packet));
        }
        return DROP;
    }

    public void switchToConfigAndThen(Runnable runnable) {
        player.transitionToConfig(server).thenAccept(_ -> {
            if (player.isConnected() && player.isConnectedToServer() && player.getServer().equals(server)) {
                runnable.run();
            }
        });
    }
}
