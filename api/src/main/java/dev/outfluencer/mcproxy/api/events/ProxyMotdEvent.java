package dev.outfluencer.mcproxy.api.events;

import dev.outfluencer.mcproxy.event.AsyncEvent;
import dev.outfluencer.mcproxy.networking.ServerStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lenni0451.lambdaevents.types.ICancellableEvent;

import java.net.SocketAddress;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class ProxyMotdEvent extends AsyncEvent implements ICancellableEvent {
    private final SocketAddress pingerAddress;
    private ServerStatus serverStatus;
    private boolean cancelled;
}
