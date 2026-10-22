package com.aero.client.core.events;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/** Tiny reflection based event bus (replaces Meteor's Orbit bus). */
public final class Events {
    private static final class Sub {
        final Object owner;
        final Method method;
        final int priority;

        Sub(Object owner, Method method, int priority) {
            this.owner = owner;
            this.method = method;
            this.priority = priority;
        }
    }

    private static final Map<Class<?>, CopyOnWriteArrayList<Sub>> SUBS = new ConcurrentHashMap<>();
    private static final Set<String> REPORTED = ConcurrentHashMap.newKeySet();

    private Events() {}

    /** Registers every {@link EventHandler} method declared on the object's class hierarchy. */
    public static synchronized void subscribe(Object owner) {
        unsubscribe(owner);
        for (Class<?> c = owner.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
            for (Method m : c.getDeclaredMethods()) {
                EventHandler h = m.getAnnotation(EventHandler.class);
                if (h == null || m.getParameterCount() != 1) continue;
                try {
                    m.setAccessible(true);
                } catch (Throwable t) {
                    continue;
                }
                Class<?> type = m.getParameterTypes()[0];
                CopyOnWriteArrayList<Sub> list = SUBS.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>());
                list.add(new Sub(owner, m, h.priority()));
                List<Sub> sorted = new ArrayList<>(list);
                sorted.sort(Comparator.comparingInt((Sub s) -> s.priority).reversed());
                list.clear();
                list.addAll(sorted);
            }
        }
    }

    public static synchronized void unsubscribe(Object owner) {
        for (CopyOnWriteArrayList<Sub> list : SUBS.values()) list.removeIf(s -> s.owner == owner);
    }

    public static boolean hasSubscribers(Class<?> eventType) {
        CopyOnWriteArrayList<Sub> list = SUBS.get(eventType);
        return list != null && !list.isEmpty();
    }

    /** Delivers the event to every subscriber of its exact type. A failing handler never crashes the game. */
    public static void post(Object event) {
        CopyOnWriteArrayList<Sub> list = SUBS.get(event.getClass());
        if (list == null || list.isEmpty()) return;
        for (Sub s : list) {
            try {
                s.method.invoke(s.owner, event);
            } catch (InvocationTargetException e) {
                report(s, e.getCause() != null ? e.getCause() : e);
            } catch (Throwable t) {
                report(s, t);
            }
        }
    }

    private static void report(Sub s, Throwable t) {
        String key = s.method.getDeclaringClass().getName() + "#" + s.method.getName() + ":" + t.getClass().getName();
        if (REPORTED.add(key)) {
            System.err.println("[Aero] Event handler " + s.method.getDeclaringClass().getSimpleName() + "." + s.method.getName() + " failed: " + t);
            t.printStackTrace();
        }
    }
}
