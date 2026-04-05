package dev.outfluencer.mcproxy.proxy.config;

import com.google.common.base.Preconditions;
import dev.outfluencer.mcproxy.api.ServerInfo;
import lombok.Getter;
import lombok.Setter;

import java.security.MessageDigest;
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
    @Setter
    private int readTimeout = 30;
    @Setter
    private int writeTimeout = 30;
    @Setter
    private int connectionThrottleLimit = 10;
    @Setter
    private long connectionThrottleMillis = 3000;
    @Setter
    private OnlineMode online = OnlineMode.AUTH;
    @Setter
    private DataForwarding dataForwarding = DataForwarding.BUNGEECORD;

    public ProxyConfig check() {
        Set<String> seen = new HashSet<>();
        for (ServerInfo server : servers) {
            Preconditions.checkState(seen.add(server.getName()), "Duplicate server name: " + server.getName());
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

    public enum OnlineMode {
        AUTH, ENCRYPT, OFFLINE;

        public boolean encrypt() {
            return this == AUTH || this == ENCRYPT;
        }

        public boolean auth() {
            return this == AUTH;
        }
    }

    public enum DataForwarding {
        NONE, BUNGEECORD;
    }

}
