package dev.outfluencer.mcproxy.api.connection;

import java.net.SocketAddress;

public interface Connection {

    String getName();

    void disconnect(String message);

    SocketAddress getAddress();

}
