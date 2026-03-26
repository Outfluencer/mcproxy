package dev.outfluencer.mcproxy.proxy.connection.handler.config;

import dev.outfluencer.mcproxy.networking.protocol.packets.config.ServerboundConfigurationPacketListener;
import dev.outfluencer.mcproxy.networking.protocol.packets.config.ServerboundFinishConfigurationPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.config.ServerboundSelectKnownPacks;
import dev.outfluencer.mcproxy.proxy.connection.PlayerImpl;
import dev.outfluencer.mcproxy.proxy.connection.handler.common.PlayerCommonPacketListener;
import dev.outfluencer.mcproxy.proxy.connection.handler.game.PlayerGamePacketListener;

public class PlayerConfigurationPacketListener extends PlayerCommonPacketListener implements ServerboundConfigurationPacketListener {

    public PlayerConfigurationPacketListener(PlayerImpl player) {
        super(player);
    }

    @Override
    public boolean handle(ServerboundFinishConfigurationPacket packet) {
        if (!player.getServer().getConfigurationTracker().pendingLoginAck) {
            return false;
        }
        player.getServer().getConfigurationTracker().pendingLoginAck = false;
        player.getConnection().setPacketListener(new PlayerGamePacketListener(player));
        return true;
    }

    @Override
    public boolean handle(ServerboundSelectKnownPacks serverboundSelectKnownPacks) {
        if (player.getServer().getConfigurationTracker().pendingKnownPacks <= 0) {
            return false;
        }
        player.getServer().getConfigurationTracker().pendingKnownPacks--;
        return true;
    }
}
