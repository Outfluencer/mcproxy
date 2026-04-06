package dev.outfluencer.mcproxy.api;

import com.google.common.base.Preconditions;
import dev.outfluencer.mcproxy.api.command.CommandManager;
import dev.outfluencer.mcproxy.api.connection.Player;
import dev.outfluencer.mcproxy.api.plugin.PluginManager;
import dev.outfluencer.mcproxy.event.EventManager;
import lombok.Getter;
import net.lenni0451.mcstructs.text.TextComponent;

import java.util.Collection;
import java.util.UUID;

public abstract class ProxyServer {

    @Getter
    private static ProxyServer instance;

    public static synchronized void setInstance(ProxyServer proxy) {
        Preconditions.checkState(instance == null, "proxy already initialized");
        instance = proxy;
    }

    public abstract PluginManager getPluginManager();
    public abstract EventManager getEventManager();
    public abstract CommandManager getCommandManager();
    public abstract String getName();
    public abstract String getVersion();

    public abstract void stop(TextComponent message);

    public abstract Player getPlayer(String name);
    public abstract Player getPlayer(UUID uuid);
    public abstract Collection<Player> getPlayers();
    public abstract int getOnlinePlayerCount();
}
