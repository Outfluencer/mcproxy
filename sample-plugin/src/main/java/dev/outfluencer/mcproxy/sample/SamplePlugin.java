package dev.outfluencer.mcproxy.sample;

import dev.outfluencer.mcproxy.api.ProxyServer;
import dev.outfluencer.mcproxy.api.plugin.Plugin;
import net.lenni0451.lambdaevents.EventHandler;

public class SamplePlugin extends Plugin {

    @Override
    public void onEnable() {
        getLogger().info("Sample plugin enabled!");
        ProxyServer.getInstance().getEventManager().register(this);
    }

    @Override
    public void onDisable() {
        getLogger().info("Sample plugin disabled!");
    }
}
