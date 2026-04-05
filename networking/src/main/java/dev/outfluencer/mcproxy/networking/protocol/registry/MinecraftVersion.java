package dev.outfluencer.mcproxy.networking.protocol.registry;

import java.util.List;

public final class MinecraftVersion {

    private MinecraftVersion() {}

    public static final int V1_21_11 = 774;
    public static final int V26_1 = 775;
    public static final List<Integer> SUPPORTED_VERSION = List.of(V1_21_11, V26_1);
}
