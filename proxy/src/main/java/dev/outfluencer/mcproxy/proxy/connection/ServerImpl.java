package dev.outfluencer.mcproxy.proxy.connection;

import dev.outfluencer.mcproxy.api.connection.Server;
import dev.outfluencer.mcproxy.networking.ConnectionHandle;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ServerImpl implements Server {
    private final PlayerImpl player;
    private final ConnectionHandle connection;
}
