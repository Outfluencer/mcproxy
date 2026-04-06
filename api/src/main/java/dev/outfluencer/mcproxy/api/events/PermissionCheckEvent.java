package dev.outfluencer.mcproxy.api.events;

import dev.outfluencer.mcproxy.api.connection.Player;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
public class PermissionCheckEvent {
    private final Player player;
    private final String permission;
    @Getter(AccessLevel.NONE)
    private boolean hasPermission;

    public boolean hasPermission() {
        return hasPermission;
    }
}
