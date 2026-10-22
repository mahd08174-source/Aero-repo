package com.aero.client.api;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Tiny reflection-based event bus: methods annotated with @EventHandler receive matching events. */
public final class EventBus {
    private record Handler(Object target, Method method, Class<?> type, int prio) {}
    private static final List<Handler> HANDLERS = new CopyOnWriteArrayList<>();

    public static void register(Object o) {
        unregister(o);
        List<Handler> found = new ArrayList<>();
        for (Class<?> c = o.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
            for (Method m : c.getDeclaredMethods()) {
                EventHandler a = m.getAnnotation(EventHandler.class);
                if (a == null || m.getParameterCount() != 1) continue;
                m.setAccessible(true);
                found.add(new Handler(o, m, m.getParameterTypes()[0], a.priority().ordinal()));
            }
        }
        HANDLERS.addAll(found);
        List<Handler> sorted = new ArrayList<>(HANDLERS);
        sorted.sort((x, y) -> Integer.compare(x.prio(), y.prio()));
        HANDLERS.clear();
        HANDLERS.addAll(sorted);
    }

    public static void unregister(Object o) { HANDLERS.removeIf(h -> h.target() == o); }

    public static void post(Object event) {
        for (Handler h : HANDLERS) {
            if (!h.type().isInstance(event)) continue;
            try { h.method().invoke(h.target(), event); }
            catch (Throwable t) { Log.error("event handler " + h.method().getName() + " failed", t); }
        }
    }
    private EventBus() {}
}
