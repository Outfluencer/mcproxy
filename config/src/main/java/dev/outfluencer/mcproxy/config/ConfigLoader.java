package dev.outfluencer.mcproxy.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigLoader {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    public static <T> T load(Path path, Class<T> type) throws IOException {
        if (Files.exists(path)) {
            T config = GSON.fromJson(Files.readString(path), type);
            // write back to persist any new fields added since last save
            save(path, config);
            return config;
        }
        T config = newInstance(type);
        save(path, config);
        return config;
    }

    public static <T> void save(Path path, T config) throws IOException {
        Files.writeString(path, GSON.toJson(config));
    }

    private static <T> T newInstance(Class<T> type) {
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Config class must have a no-arg constructor", e);
        }
    }
}
