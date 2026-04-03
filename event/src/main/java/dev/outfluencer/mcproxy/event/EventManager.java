package dev.outfluencer.mcproxy.event;

import net.lenni0451.lambdaevents.LambdaManager;
import net.lenni0451.lambdaevents.generator.LambdaMetaFactoryGenerator;
import net.lenni0451.lambdaevents.utils.ThrowingExceptionHandler;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

public class EventManager {
    private static final Logger logger = Logger.getLogger(EventManager.class.getName());
    private final LambdaManager lambdaManager;

    {
        MethodHandles.Lookup lookup;
        try {
            Field implLookup = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            implLookup.setAccessible(true);
            lookup = (MethodHandles.Lookup) implLookup.get(null);
        } catch (Exception e) {
            lookup = MethodHandles.lookup();
        }

        lambdaManager = LambdaManager.basic(new LambdaMetaFactoryGenerator(lookup));
        lambdaManager.setExceptionHandler(new ThrowingExceptionHandler());
    }

    public void register(Object listener) {
        synchronized (this) {
            lambdaManager.register(listener);
        }
    }

    public void unregister(Object listener) {
        synchronized (this) {
            lambdaManager.unregister(listener);
        }
    }

    public <T> T fire(T event) {
        return lambdaManager.call(event);
    }

    public <T extends AsyncEvent> void fireAsync(T event, BiConsumer<T, Throwable> callback, Executor executor) {
        fire(event).whenComplete((BiConsumer<AsyncEvent, Throwable>) callback, executor);
    }
}
