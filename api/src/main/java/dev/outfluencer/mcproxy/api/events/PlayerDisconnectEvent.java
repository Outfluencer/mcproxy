package dev.outfluencer.mcproxy.api.events;

import dev.outfluencer.mcproxy.api.connection.Player;
import lombok.AllArgsConstructor;

public record PlayerDisconnectEvent(Player player) {
}
