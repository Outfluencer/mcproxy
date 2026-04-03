package dev.outfluencer.mcproxy.sample;

import dev.outfluencer.mcproxy.api.ProxyServer;
import dev.outfluencer.mcproxy.api.events.CompressionChangeEvent;
import dev.outfluencer.mcproxy.api.events.unsafe.ChannelInitializedEvent;
import dev.outfluencer.mcproxy.api.plugin.Plugin;
import net.lenni0451.lambdaevents.EventHandler;

public class SamplePlugin extends Plugin {

    @Override
    public void onEnable() {
        getLogger().info("Sample plugin enabled!");
        ProxyServer.getInstance().getEventManager().register(this);
    }

    @EventHandler
    public void onChannelInit(ChannelInitializedEvent event) {
        getLogger().info(String.valueOf(event));
    }

    @EventHandler
    public void onCompression(CompressionChangeEvent event) {
        getLogger().info(String.valueOf(event));
    }

    @Override
    public void onDisable() {
        getLogger().info("Sample plugin disabled!");
    }
}
