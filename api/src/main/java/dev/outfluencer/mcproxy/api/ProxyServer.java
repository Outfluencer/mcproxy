package dev.outfluencer.mcproxy.api;

import dev.outfluencer.mcproxy.api.connection.Player;
import dev.outfluencer.mcproxy.event.EventManager;
import lombok.Getter;

import java.util.Collection;
import java.util.UUID;

public abstract class ProxyServer {

    @Getter
    private static ProxyServer instance;

    public static synchronized void setInstance(ProxyServer proxy) {
        if(instance != null) {
            throw new IllegalStateException();
        }
        instance = proxy;
    }

    public abstract EventManager getEventManager();
    public abstract String getName();
    public abstract String getVersion();

    public abstract void stop();

    public abstract Player getPlayer(String name);
    public abstract Player getPlayer(UUID uuid);
    public abstract Collection<Player> getPlayers();
    public abstract int getOnlinePlayerCount();
}
