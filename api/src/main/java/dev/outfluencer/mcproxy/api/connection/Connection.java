package dev.outfluencer.mcproxy.api.connection;

import java.net.InetSocketAddress;

public interface Connection {

    String getName();

    void disconnect(String message);

    InetSocketAddress getAddress();

}
