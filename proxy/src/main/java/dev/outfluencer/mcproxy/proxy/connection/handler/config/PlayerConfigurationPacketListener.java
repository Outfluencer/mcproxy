package dev.outfluencer.mcproxy.proxy.connection.handler.config;

import com.google.common.base.Preconditions;
import dev.outfluencer.mcproxy.networking.protocol.packets.config.ServerboundConfigurationPacketListener;
import dev.outfluencer.mcproxy.networking.protocol.packets.config.ServerboundFinishConfigurationPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.config.ServerboundSelectKnownPacks;
import dev.outfluencer.mcproxy.networking.protocol.registry.Protocol;
import dev.outfluencer.mcproxy.proxy.connection.PlayerImpl;
import dev.outfluencer.mcproxy.proxy.connection.handler.common.PlayerCommonPacketListener;
import dev.outfluencer.mcproxy.proxy.connection.handler.game.PlayerGamePacketListener;

public class PlayerConfigurationPacketListener extends PlayerCommonPacketListener implements ServerboundConfigurationPacketListener {

    public PlayerConfigurationPacketListener(PlayerImpl player) {
        assert player.getDecoderProtocol() == Protocol.CONFIG;
        super(player);
    }

    @Override
    public boolean handle(ServerboundFinishConfigurationPacket packet) {
        player.getConfigurationTracker().setPendingFinishConfiguration(false);
        player.getConnection().setPacketListener(new PlayerGamePacketListener(player));
        if (!getServer().getConfigurationTracker().isPendingLoginAck()) {
            return DROP;
        }
        getServer().getConfigurationTracker().setPendingLoginAck(false);
        if(player.getSettings() != null) {
            getServer().sendPacket(player.getSettings());
        }
        return PASS;
    }

    @Override
    public boolean handle(ServerboundSelectKnownPacks serverboundSelectKnownPacks) {
        if (getServer().getConfigurationTracker().getPendingKnownPacks().get() <= 0) {
            return DROP;
        }
        getServer().getConfigurationTracker().getPendingKnownPacks().decrement();
        return PASS;
    }
}
