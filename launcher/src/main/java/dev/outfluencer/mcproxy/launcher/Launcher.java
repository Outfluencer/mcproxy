package dev.outfluencer.mcproxy.launcher;

import dev.outfluencer.mcproxy.proxy.MinecraftProxy;

public class Launcher {

    public static void main(String[] args) {
        if (Float.parseFloat(System.getProperty("java.class.version")) < 69.0) {
            System.err.println("mcproxy requires Java 25 to start.");
            return;
        }

        MinecraftProxy.start();
    }

}
