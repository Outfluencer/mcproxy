package dev.outfluencer.mcproxy.api.command;

import net.lenni0451.mcstructs.text.TextComponent;

public interface CommandSource {

    void sendMessage(String message);

    void sendMessage(TextComponent component);

    String getName();

    boolean hasPermission(String permission);
}
