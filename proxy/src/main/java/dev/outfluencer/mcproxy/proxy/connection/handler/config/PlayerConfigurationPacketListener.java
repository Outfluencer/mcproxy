package dev.outfluencer.mcproxy.proxy.connection.handler.config;

import dev.outfluencer.mcproxy.networking.protocol.packets.config.ServerboundConfigurationPacketListener;
import dev.outfluencer.mcproxy.networking.protocol.packets.config.ServerboundFinishConfigurationPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.config.ServerboundSelectKnownPacks;
import dev.outfluencer.mcproxy.networking.protocol.registry.Protocol;
import dev.outfluencer.mcproxy.proxy.connection.PlayerImpl;
import dev.outfluencer.mcproxy.proxy.connection.handler.common.PlayerCommonPacketListener;
import dev.outfluencer.mcproxy.proxy.connection.handler.game.PlayerGamePacketListener;

public class PlayerConfigurationPacketListener extends PlayerCommonPacketListener implements ServerboundConfigurationPacketListener {

    public PlayerConfigurationPacketListener(PlayerImpl player) {
        super(player);
        assert player.getDecoderProtocol() == Protocol.CONFIG;
    }

    @Override
    public boolean handle(ServerboundFinishConfigurationPacket packet) {
        player.getConnection().setPacketListener(new PlayerGamePacketListener(player));
        if (!getServer().getConfigurationTracker().pendingLoginAck) {
            return false;
        }
        getServer().getConfigurationTracker().pendingLoginAck = false;
        return true;
    }

    @Override
    public boolean handle(ServerboundSelectKnownPacks serverboundSelectKnownPacks) {
        if (getServer().getConfigurationTracker().pendingKnownPacks <= 0) {
            return false;
        }
        getServer().getConfigurationTracker().pendingKnownPacks--;
        return true;
    }
}
