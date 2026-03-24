package dev.outfluencer.mcproxy.proxy.connection;

import dev.outfluencer.mcproxy.api.connection.Player;
import dev.outfluencer.mcproxy.networking.ConnectionHandle;
import lombok.Getter;
import lombok.Setter;

import java.net.InetSocketAddress;
import java.util.UUID;

@Getter
@Setter
public class PlayerImpl implements Player {

    private final ConnectionHandle connection;

    private String name;
    private UUID uuid;
    private ServerImpl server;

    public PlayerImpl(ConnectionHandle connectionHandle, String name, UUID uuid) {
        this.connection = connectionHandle;
        this.name = name;
        this.uuid = uuid;
    }

    @Override
    public void disconnect(String message) {
        connection.secureClose(null);
    }

    @Override
    public InetSocketAddress getAddress() {
        return (InetSocketAddress) connection.getChannel().remoteAddress();
    }

    public void connect(InetSocketAddress address) {
        new BackendConnector(this, address).connect();
    }

    private boolean bundling;

    public void toggleBundle() {
        bundling = !bundling;
    }

}
