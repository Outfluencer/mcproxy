package dev.outfluencer.mcproxy.api.connection;

import dev.outfluencer.mcproxy.api.ServerInfo;

import java.util.UUID;

public interface Player extends Connection {
    UUID getUuid();
    Server getServer();
    void connect(ServerInfo serverInfo);
}
