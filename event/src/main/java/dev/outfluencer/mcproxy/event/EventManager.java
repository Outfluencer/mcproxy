package dev.outfluencer.mcproxy.event;

import net.lenni0451.lambdaevents.LambdaManager;
import net.lenni0451.lambdaevents.generator.LambdaMetaFactoryGenerator;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

public class EventManager {

    private final LambdaManager lambdaManager;

    {
        MethodHandles.Lookup lookup;
        try {
            Field implLookup = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            implLookup.setAccessible(true);
            lookup = (MethodHandles.Lookup) implLookup.get(null);
        } catch (ReflectiveOperationException e) {
            lookup = MethodHandles.lookup();
            e.printStackTrace();
        }

        lambdaManager = LambdaManager.basic(new LambdaMetaFactoryGenerator(lookup));
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

    public void fire(Object event) {
        lambdaManager.call(event);
    }
}
