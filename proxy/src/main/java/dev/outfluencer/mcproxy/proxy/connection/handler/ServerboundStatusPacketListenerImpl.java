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
public class ServerboundStatusPacketListenerImpl implements ServerboundStatusPacketListener {

    private final ConnectionHandle handle;
    private State state = State.AWAIT_STATUS;

    private enum State {
        AWAIT_STATUS, RECEIVED_STATUS, AWAIT_PING, RECEIVED_PING
    }

    @Override
    public boolean handle(ServerboundStatusRequestPacket packet) {
        if (state != State.AWAIT_STATUS) {
            throw new IllegalStateException("Unexpected ServerboundStatusRequestPacket");
        }
        state = State.RECEIVED_STATUS;
        handle.sendPacket(new ClientboundStatusResponsePacket(new ServerStatus(new ServerStatus.Version("mcproxy test server", MinecraftVersion.V26_1), null, new StringComponent("mcproxy test server"))));
        state = State.AWAIT_PING;
        return false;
    }

    @Override
    public boolean handle(ServerboundPingRequest packet) {
        if (state != State.AWAIT_PING) {
            throw new IllegalStateException("Unexpected ServerboundStatusRequestPacket");
        }
        state = State.RECEIVED_PING;
        handle.close(new ClientboundPongResponsePacket(packet.getPing()));
        return false;
    }

    @Override
    public void handle(DecodedPacket decodedPacket) {
        throw new IllegalStateException("Unexpected DecodedPacket");
    }
}
