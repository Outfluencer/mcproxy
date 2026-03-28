package dev.outfluencer.mcproxy.config;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class ProxyConfig {

    private String bind = "0.0.0.0";
    private int port = 25577;
    private List<ServerInfo> servers = defaultServers();
    @Setter
    private String motd = "A mcproxy server";
    @Setter
    private int maxPlayers = 1;
    @Setter
    private int compressionThreshold = 256;
    private int readTimeout = 30;
    private int writeTimeout = 30;
    private int connectionThrottleLimit = 10;
    private long connectionThrottleMillis = 3000;

    public ProxyConfig check() {
        Set<String> seen = new HashSet<>();
        for (ServerInfo server : servers) {
            if (!seen.add(server.getName())) {
                throw new IllegalStateException("Duplicate server name: " + server.getName());
            }
        }
        servers = new CopyOnWriteArrayList<>(servers);
        return this;
    }

    public ServerInfo getServer(String name) {
        for (ServerInfo server : servers) {
            if (server.getName().equals(name)) {
                return server;
            }
        }
        return null;
    }

    public List<ServerInfo> getFallbackServers() {
        List<ServerInfo> fallback = new ArrayList<>();
        for (ServerInfo server : servers) {
            if (server.isFallback()) {
                fallback.add(server);
            }
        }
        Collections.shuffle(fallback);
        fallback.sort(Comparator.comparingInt(ServerInfo::getPriority));
        return fallback;
    }

    private static List<ServerInfo> defaultServers() {
        List<ServerInfo> list = new ArrayList<>();
        list.add(new ServerInfo("lobby-1", "127.0.0.1", 25565, 1, true, null));
        list.add(new ServerInfo("lobby-2", "127.0.0.1", 25566, 1, true, null));
        return list;
    }
}
