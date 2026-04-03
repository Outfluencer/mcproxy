package dev.outfluencer.mcproxy.api.events;

import dev.outfluencer.mcproxy.api.connection.InitialPlayer;
import dev.outfluencer.mcproxy.event.AsyncEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lenni0451.lambdaevents.types.ICancellableEvent;
import net.lenni0451.mcstructs.text.TextComponent;

@Data
@EqualsAndHashCode(callSuper = true)
public class PlayerLoginEvent extends AsyncEvent implements ICancellableEvent {
    private final InitialPlayer player;
    private boolean cancelled;
    private TextComponent disconnectMessage;
}
