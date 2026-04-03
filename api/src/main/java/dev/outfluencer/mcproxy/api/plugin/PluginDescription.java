package dev.outfluencer.mcproxy.api.plugin;

import java.util.List;

public record PluginDescription(String name, String version, String mainClass, List<String> authors) {

}