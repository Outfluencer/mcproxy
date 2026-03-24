package dev.outfluencer.mcproxy.api.connection;

import java.util.UUID;

public interface Player extends Connection {
    UUID getUuid();
}
