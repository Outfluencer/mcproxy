package dev.outfluencer.mcproxy.api.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import dev.outfluencer.mcproxy.api.util.ComponentBuilder;
import lombok.Getter;
import net.lenni0451.mcstructs.text.TextComponent;

import java.awt.*;
import java.util.concurrent.CompletableFuture;

@Getter
public class CommandManager {

    private final CommandDispatcher<CommandSource> dispatcher = new CommandDispatcher<>();

    public void register(LiteralArgumentBuilder<CommandSource> command) {
        dispatcher.register(command);
    }

    public boolean execute(String input, CommandSource source) {
        ParseResults<CommandSource> parse = dispatcher.parse(input, source);
        if (parse.getReader().canRead() && parse.getContext().getNodes().isEmpty()) {
            return false;
        }
        try {
            dispatcher.execute(parse);
        } catch (CommandSyntaxException e) {
            source.sendMessage(ComponentBuilder.gradient(e.getMessage(), Color.RED, new Color(151, 33, 255)).build());
        }
        return true;
    }

    public CompletableFuture<Suggestions> tabComplete(String input, CommandSource source) {
        ParseResults<CommandSource> parse = dispatcher.parse(input, source);
        return dispatcher.getCompletionSuggestions(parse);
    }

}
