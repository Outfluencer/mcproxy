package dev.outfluencer.mcproxy.event;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class AsyncEvent {

    private final CopyOnWriteArrayList<CompletableFuture<Void>> intents = new CopyOnWriteArrayList<>();

    public void registerIntent(CompletableFuture<Void> future) {
        intents.add(future);
    }

    public void whenComplete(BiConsumer<AsyncEvent, Throwable> callback, Executor executor) {
        if (intents.isEmpty()) {
            callback.accept(this, null);
            return;
        }
        CompletableFuture.allOf(intents.toArray(CompletableFuture[]::new))
                .whenCompleteAsync((_, throwable) -> callback.accept(this, throwable), executor);
    }
}
