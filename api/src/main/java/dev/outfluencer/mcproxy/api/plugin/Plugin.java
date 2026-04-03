package dev.outfluencer.mcproxy.api.plugin;

import lombok.Getter;

import java.util.logging.Logger;

@Getter
public abstract class Plugin {

    private PluginDescription description;
    private Logger logger;

    public void init(PluginDescription description, Logger logger) {
        this.description = description;
        this.logger = logger;
    }

    public abstract void onEnable();

    public void onDisable() {
    }
}