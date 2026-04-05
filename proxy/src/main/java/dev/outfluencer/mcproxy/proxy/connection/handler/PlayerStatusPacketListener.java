package dev.outfluencer.mcproxy.proxy.connection.handler;

import dev.outfluencer.mcproxy.api.events.ProxyMotdEvent;
import dev.outfluencer.mcproxy.api.util.ComponentBuilder;
import dev.outfluencer.mcproxy.networking.ConnectionHandle;
import dev.outfluencer.mcproxy.networking.ServerStatus;
import dev.outfluencer.mcproxy.networking.netty.QuietException;
import dev.outfluencer.mcproxy.networking.protocol.packets.status.ClientboundPongResponsePacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.status.ClientboundStatusResponsePacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.status.ServerboundPingRequest;
import dev.outfluencer.mcproxy.networking.protocol.packets.status.ServerboundStatusPacketListener;
import dev.outfluencer.mcproxy.networking.protocol.packets.status.ServerboundStatusRequestPacket;
import dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion;
import dev.outfluencer.mcproxy.proxy.MinecraftProxy;
import dev.outfluencer.mcproxy.proxy.config.ProxyConfig;
import lombok.RequiredArgsConstructor;
import net.lenni0451.mcstructs.text.components.StringComponent;

import java.awt.*;

@RequiredArgsConstructor
public class PlayerStatusPacketListener implements ServerboundStatusPacketListener {

    private final MinecraftProxy proxy = MinecraftProxy.getInstance();
    private final ConnectionHandle connection;
    private State state = State.AWAIT_STATUS;

    private enum State {
        AWAIT_STATUS, RECEIVED_STATUS, AWAIT_PING, RECEIVED_PING
    }

    @Override
    public boolean handle(ServerboundStatusRequestPacket packet) {
        stateTransition(State.AWAIT_STATUS, State.RECEIVED_STATUS, "Unexpected ServerboundStatusRequestPacket");
        proxy.getEventManager().fireAsync(new ProxyMotdEvent(connection.getAddress(), getServerStatus(), false), connection.eventCallback(e -> {
            if (e.isCancelled()) {
                connection.close(null);
                return;
            }
            connection.sendPacket(new ClientboundStatusResponsePacket(e.getServerStatus()));
            state = State.AWAIT_PING;
        }), connection.getChannel().eventLoop());
        return false;
    }

    private ServerStatus getServerStatus() {
        int protocolVersion = connection.getProtocolVersion();
        boolean supported = MinecraftVersion.isSupported(protocolVersion);
        ProxyConfig config = proxy.getConfig();
        var players = new ServerStatus.Players(config.getMaxPlayers(), proxy.getOnlinePlayerCount(), new ServerStatus.Player[0]);
        return new ServerStatus(new ServerStatus.Version(proxy.getName(), supported ? protocolVersion : MinecraftVersion.SUPPORTED_VERSION.getLast()), players, new StringComponent(config.getMotd()));
    }

    @Override
    public boolean handle(ServerboundPingRequest packet) {
        stateTransition(State.AWAIT_PING, State.RECEIVED_PING, "Unexpected ServerboundPingRequest");
        connection.close(new ClientboundPongResponsePacket(packet.getPing()));
        return false;
    }

    private void stateTransition(State expected, State next, String errorMessage) {
        if (state == expected) {
            state = next;
        } else {
            throw new QuietException(errorMessage);
        }
    }

    @Override
    public String toString() {
        return "[" + getClass().getSimpleName() + "|" + connection.getAddress() + "]";
    }

}
