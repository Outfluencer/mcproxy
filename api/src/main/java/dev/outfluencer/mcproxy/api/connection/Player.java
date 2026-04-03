package dev.outfluencer.mcproxy.api.connection;

import dev.outfluencer.mcproxy.api.ServerInfo;

public interface Player extends InitialPlayer {
    Server getServer();
    void connect(ServerInfo serverInfo);
}
