package dev.outfluencer.mcproxy.api.connection;

import java.util.UUID;

public interface InitialPlayer extends Connection {
    UUID getUuid();
}
