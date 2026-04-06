package dev.outfluencer.mcproxy.api.events;

import dev.outfluencer.mcproxy.api.connection.Player;
import dev.outfluencer.mcproxy.event.AsyncEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import net.lenni0451.lambdaevents.types.ICancellableEvent;
import net.lenni0451.mcstructs.text.TextComponent;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PlayerLoggedInEvent extends AsyncEvent implements ICancellableEvent {
    private final Player player;
    private boolean cancelled;
    private TextComponent disconnectMessage;
}
