package dev.outfluencer.mcproxy.api.events;

import dev.outfluencer.mcproxy.api.ServerInfo;
import dev.outfluencer.mcproxy.api.connection.Player;
import dev.outfluencer.mcproxy.api.connection.Server;
import lombok.Data;
import net.lenni0451.lambdaevents.types.ICancellableEvent;
import net.lenni0451.mcstructs.text.TextComponent;

@Data
public class ServerKickPlayerEvent implements ICancellableEvent {
    private final Player player;
    private final Server server;
    private TextComponent reason;
    private ServerInfo fallbackServer;
    private boolean cancelled;

    public ServerKickPlayerEvent(Player player, Server server, TextComponent reason) {
        this.player = player;
        this.server = server;
        this.reason = reason;
    }
}
