package dev.outfluencer.mcproxy.proxy.connection.handler;

import dev.outfluencer.mcproxy.networking.ConnectionHandle;
import dev.outfluencer.mcproxy.networking.ServerStatus;
import dev.outfluencer.mcproxy.networking.protocol.DecodedPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.status.ClientboundPongResponsePacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.status.ClientboundStatusResponsePacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.status.ServerboundPingRequest;
import dev.outfluencer.mcproxy.networking.protocol.packets.status.ServerboundStatusPacketListener;
import dev.outfluencer.mcproxy.networking.protocol.packets.status.ServerboundStatusRequestPacket;
import dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion;
import lombok.RequiredArgsConstructor;
import net.lenni0451.mcstructs.text.components.StringComponent;

@RequiredArgsConstructor
public class PlayerStatusPacketListener implements ServerboundStatusPacketListener {

    private final ConnectionHandle handle;
    private State state = State.AWAIT_STATUS;

    private enum State {
        AWAIT_STATUS, RECEIVED_STATUS, AWAIT_PING, RECEIVED_PING
    }

    @Override
    public boolean handle(ServerboundStatusRequestPacket packet) {
        stateTransition(State.AWAIT_STATUS, State.RECEIVED_STATUS, "Unexpected ServerboundStatusRequestPacket");
        handle.sendPacket(new ClientboundStatusResponsePacket(new ServerStatus(new ServerStatus.Version("mcproxy test server", MinecraftVersion.V26_1), null, new StringComponent("mcproxy test server"))));
        state = State.AWAIT_PING;
        return false;
    }

    @Override
    public boolean handle(ServerboundPingRequest packet) {
        stateTransition(State.AWAIT_PING, State.RECEIVED_PING, "Unexpected ServerboundPingRequest");
        handle.close(new ClientboundPongResponsePacket(packet.getPing()));
        return false;
    }

    @Override
    public void handle(DecodedPacket decodedPacket) {
        throw new IllegalStateException("Unexpected DecodedPacket");
    }

    private void stateTransition(State expected, State next, String errorMessage) {
        if (state == expected) {
            state = next;
        } else  {
            throw new IllegalStateException(errorMessage);
        }
    }
}
