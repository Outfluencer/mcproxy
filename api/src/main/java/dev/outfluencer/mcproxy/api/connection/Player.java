package dev.outfluencer.mcproxy.api.connection;

import dev.outfluencer.mcproxy.api.ServerInfo;
import dev.outfluencer.mcproxy.api.command.CommandSource;

public interface Player extends InitialPlayer, CommandSource {
    Server getServer();
    void connect(ServerInfo serverInfo);
}
