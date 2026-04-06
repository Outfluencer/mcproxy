package dev.outfluencer.mcproxy.proxy.command;

import dev.outfluencer.mcproxy.api.command.CommandSource;
import dev.outfluencer.mcproxy.proxy.MinecraftProxy;
import net.lenni0451.mcstructs.text.TextComponent;

public class ConsoleCommandSource implements CommandSource {

    public static final ConsoleCommandSource INSTANCE = new ConsoleCommandSource();

    private ConsoleCommandSource() {
    }

    @Override
    public void sendMessage(String message) {
        MinecraftProxy.getLogger().info(message);
    }

    @Override
    public void sendMessage(TextComponent component) {
        sendMessage(component.asUnformattedString());
    }

    @Override
    public String getName() {
        return "CONSOLE";
    }

    @Override
    public boolean hasPermission(String permission) {
        return true;
    }
}
