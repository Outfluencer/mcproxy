package dev.outfluencer.mcproxy.proxy.plugin;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import dev.outfluencer.mcproxy.api.plugin.Plugin;
import dev.outfluencer.mcproxy.api.plugin.PluginDescription;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

public class PluginLoader {

    private static final Logger logger = Logger.getLogger(PluginLoader.class.getName());
    private static final Gson GSON = new Gson();

    private final List<Plugin> plugins = new ArrayList<>();
    private final Path pluginsDir;

    public PluginLoader(Path pluginsDir) {
        this.pluginsDir = pluginsDir;
    }

    public void loadPlugins() throws IOException {
        if (!Files.isDirectory(pluginsDir)) {
            Files.createDirectories(pluginsDir);
            return;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(pluginsDir, "*.jar")) {
            for (Path jarPath : stream) {
                try {
                    Plugin plugin = loadPlugin(jarPath);
                    plugins.add(plugin);
                } catch (Exception e) {
                    logger.severe("Failed to load plugin from " + jarPath.getFileName() + ": " + e.getMessage());
                }
            }
        }
    }

    private Plugin loadPlugin(Path jarPath) throws Exception {
        PluginDescription description;
        try (JarFile jarFile = new JarFile(jarPath.toFile())) {
            JarEntry entry = jarFile.getJarEntry("mcproxy.json");
            if (entry == null) {
                throw new IllegalArgumentException("Missing mcproxy.json");
            }

            try (InputStreamReader reader = new InputStreamReader(jarFile.getInputStream(entry))) {
                description = parseDescription(reader);
            }
        }

        URLClassLoader classLoader = new URLClassLoader(
                new URL[]{jarPath.toUri().toURL()},
                getClass().getClassLoader()
        );

        Class<?> mainClass = classLoader.loadClass(description.mainClass());
        if (!Plugin.class.isAssignableFrom(mainClass)) {
            throw new IllegalArgumentException("Main class " + description.mainClass() + " does not extend Plugin");
        }

        Plugin plugin = (Plugin) mainClass.getDeclaredConstructor().newInstance();
        plugin.init(description, Logger.getLogger(description.name()));
        return plugin;
    }

    private PluginDescription parseDescription(InputStreamReader reader) {
        return GSON.fromJson(reader, PluginDescription.class);
    }

    public void enablePlugins() {
        for (Plugin plugin : plugins) {
            try {
                plugin.onEnable();
                logger.info("Enabled plugin " + plugin.getDescription().name()
                        + " v" + plugin.getDescription().version());
            } catch (Exception e) {
                logger.severe("Failed to enable plugin " + plugin.getDescription().name() + ": " + e.getMessage());
            }
        }
    }

    public void disablePlugins() {
        for (Plugin plugin : plugins) {
            try {
                plugin.onDisable();
                logger.info("Disabled plugin " + plugin.getDescription().name());
            } catch (Exception e) {
                logger.severe("Failed to disable plugin " + plugin.getDescription().name() + ": " + e.getMessage());
            }
        }
    }

    public List<Plugin> getPlugins() {
        return Collections.unmodifiableList(plugins);
    }
}