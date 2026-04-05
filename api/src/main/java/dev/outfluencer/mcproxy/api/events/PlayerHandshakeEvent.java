package dev.outfluencer.mcproxy.api.events;

import dev.outfluencer.mcproxy.networking.protocol.packets.handshake.ServerboundHandshakePacket;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.lenni0451.lambdaevents.types.ICancellableEvent;
import net.lenni0451.mcstructs.text.TextComponent;

import java.net.SocketAddress;

@Data
@RequiredArgsConstructor
public class PlayerHandshakeEvent implements ICancellableEvent {
    private final SocketAddress address;
    private final ServerboundHandshakePacket packet;
    private boolean cancelled;
    private TextComponent disconnectMessage;
}
