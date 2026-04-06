package dev.outfluencer.mcproxy.proxy.connection.handler.config;

import dev.outfluencer.mcproxy.networking.protocol.DecodedPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.Packet;
import dev.outfluencer.mcproxy.networking.protocol.packets.common.ClientboundUpdateTagsPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.config.ClientboundConfigurationPacketListener;
import dev.outfluencer.mcproxy.networking.protocol.packets.config.ClientboundFinishConfigurationPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.config.ClientboundRegistryDataPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.config.ClientboundSelectKnownPacks;
import dev.outfluencer.mcproxy.networking.protocol.registry.Protocol;
import dev.outfluencer.mcproxy.proxy.connection.ServerImpl;
import dev.outfluencer.mcproxy.proxy.connection.handler.common.ServerCommonPacketListener;
import dev.outfluencer.mcproxy.proxy.connection.handler.game.ServerGamePacketListener;

import java.util.LinkedList;
import java.util.Queue;

public class ServerConfigurationPacketListener extends ServerCommonPacketListener implements ClientboundConfigurationPacketListener {

    /*
     * This queue is used to accumulate all registry data and update tag packets.
     * We send them all at once just before the FinishConfiguration.
     * As otherwise during sever switches, when the player already was in config state,
     * the client can be in an irrecoverable state.
     */
    private final Queue<Packet<?>> registryAccumulationQueue = new LinkedList<>();

    public ServerConfigurationPacketListener(ServerImpl server) {
        assert server.getDecoderProtocol() == Protocol.CONFIG;
        super(server);
        server.getConfigurationTracker().assertSafe();
    }

    @Override
    public void handle(DecodedPacket decodedPacket) {
        player.sendDecodedPacket(decodedPacket);
    }

    @Override
    public boolean handle(ClientboundFinishConfigurationPacket packet) {
        server.getConfigurationTracker().setPendingLoginAck(true);
        // send all possibly config state breaking packets at the same time.
        while (!registryAccumulationQueue.isEmpty()) {
            player.sendPacket(registryAccumulationQueue.poll());
        }
        server.getConnection().setPacketListener(new ServerGamePacketListener(server));
        player.getConfigurationTracker().setPendingFinishConfiguration(true);
        return PASS;
    }

    @Override
    public boolean handle(ClientboundRegistryDataPacket packet) {
        registryAccumulationQueue.add(packet);
        return DROP;
    }

    @Override
    public boolean handle(ClientboundUpdateTagsPacket packet) {
        registryAccumulationQueue.add(packet);
        return DROP;
    }

    @Override
    public boolean handle(ClientboundSelectKnownPacks clientboundSelectKnownPacks) {
        server.getConfigurationTracker().getPendingKnownPacks().increment();
        return PASS;
    }
}
