package dev.outfluencer.mcproxy.sample;

import dev.outfluencer.mcproxy.api.ProxyServer;
import dev.outfluencer.mcproxy.api.events.CompressionChangeEvent;
import dev.outfluencer.mcproxy.api.events.ProxyMotdEvent;
import dev.outfluencer.mcproxy.api.events.ServerKickPlayerEvent;
import dev.outfluencer.mcproxy.api.events.unsafe.ChannelInitializedEvent;
import dev.outfluencer.mcproxy.api.plugin.Plugin;
import dev.outfluencer.mcproxy.api.util.ComponentBuilder;
import net.lenni0451.lambdaevents.EventHandler;
import net.lenni0451.mcstructs.text.Style;
import net.lenni0451.mcstructs.text.TextComponent;
import net.lenni0451.mcstructs.text.utils.TextComponentBuilder;

import java.awt.*;

public class SamplePlugin extends Plugin {

    public static final TextComponent MOTD = ComponentBuilder.gradient("mcproxy test server", Color.red, Color.blue).bold().build();

    @Override
    public void onEnable() {
        getLogger().info("Sample plugin enabled!");
        ProxyServer.getInstance().getEventManager().register(this);
    }
    @EventHandler
    public void onMotd(ProxyMotdEvent event) {
        event.getServerStatus().setDescription(MOTD);
    }

    @EventHandler
    public void onPlayerKick(ServerKickPlayerEvent event) {
        event.getPlayer().sendMessage(event.getReason());
        event.setCancelled(true);
    }


    @Override
    public void onDisable() {
        getLogger().info("Sample plugin disabled!");
    }
}
