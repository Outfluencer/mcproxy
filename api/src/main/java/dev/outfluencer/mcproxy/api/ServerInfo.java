package dev.outfluencer.mcproxy.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.net.InetSocketAddress;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ServerInfo {

    private String name;
    private String address = "127.0.0.1";
    private int port = 25565;
    private int priority;
    private boolean fallback;

    private transient InetSocketAddress socketAddress;

    public InetSocketAddress getSocketAddress() {
        if (socketAddress == null) {
            socketAddress = new InetSocketAddress(address, port);
        }
        return socketAddress;
    }
}
