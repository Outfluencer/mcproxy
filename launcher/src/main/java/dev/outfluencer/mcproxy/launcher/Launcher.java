package dev.outfluencer.mcproxy.launcher;

import dev.outfluencer.mcproxy.proxy.MinecraftProxy;

import java.util.logging.Level;

public class Launcher {

    public static void main(String[] args) {
        if (Float.parseFloat(System.getProperty("java.class.version")) < 69.0) {
            System.err.println("mcproxy requires Java 25 to start.");
            return;
        }

        long start = System.currentTimeMillis();
        MinecraftProxy.getLogger().log(Level.INFO, "Proxy started in {0}ms", (System.currentTimeMillis() - start));
    }

}
