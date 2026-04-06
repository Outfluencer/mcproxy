package dev.outfluencer.mcproxy.api.plugin;

import java.util.List;

public interface PluginManager {
    Plugin getPlugin(Class<?> pluginClass);

    List<Plugin> getPlugins();

    Plugin getPlugin(String name);

    boolean hasPlugin(Class<?> pluginClass);

    boolean hasPlugin(String name);
}
