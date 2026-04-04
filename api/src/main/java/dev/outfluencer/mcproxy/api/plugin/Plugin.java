package dev.outfluencer.mcproxy.api.plugin;

import lombok.Getter;

import java.nio.file.Path;
import java.util.logging.Logger;

@Getter
public abstract class Plugin {

    private PluginDescription description;
    private Logger logger;
    private Path pluginFolder;

    public void init(PluginDescription description, Logger logger) {
        this.description = description;
        this.logger = logger;
        this.pluginFolder = Path.of("plugins", description.name());
    }

    public abstract void onEnable();

    public void onDisable() {
    }
}