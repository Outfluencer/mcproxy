package dev.outfluencer.mcproxy.api.events;

import dev.outfluencer.mcproxy.api.ServerInfo;
import dev.outfluencer.mcproxy.api.connection.Player;
import dev.outfluencer.mcproxy.api.connection.Server;
import lombok.Data;
import lombok.NonNull;
import net.lenni0451.lambdaevents.types.ICancellableEvent;
import net.lenni0451.mcstructs.text.TextComponent;

@Data
public class ServerKickPlayerEvent implements ICancellableEvent {
    private @NonNull
    final Player player;
    private @NonNull
    final Server server;
    private @NonNull TextComponent reason;
    private ServerInfo fallbackServer;
    private boolean cancelled;

    public ServerKickPlayerEvent(@NonNull Player player, @NonNull Server server, @NonNull TextComponent reason) {
        this.player = player;
        this.server = server;
        this.reason = reason;
    }
}
