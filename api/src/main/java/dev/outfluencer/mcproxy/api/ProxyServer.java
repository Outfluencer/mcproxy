package dev.outfluencer.mcproxy.api;

import dev.outfluencer.mcproxy.event.EventManager;
import lombok.Getter;

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
}
