package com.aero.client.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Registry of all modules. */
public final class Modules {
    private static final Modules INSTANCE = new Modules();
    private static final List<Category> CATEGORIES = new ArrayList<>();

    private final Map<String, Module> byName = new LinkedHashMap<>();

    private Modules() {}

    public static Modules get() { return INSTANCE; }

    public static void registerCategory(Category category) {
        if (!CATEGORIES.contains(category)) CATEGORIES.add(category);
    }

    public static List<Category> getCategories() { return Collections.unmodifiableList(CATEGORIES); }

    public void add(Module module) { byName.put(module.name, module); }

    public Module get(String name) { return byName.get(name); }

    public <T extends Module> T get(Class<T> type) {
        for (Module m : byName.values()) if (m.getClass() == type) return type.cast(m);
        return null;
    }

    public List<Module> getAll() { return new ArrayList<>(byName.values()); }

    public List<Module> getGroup(Category category) {
        List<Module> out = new ArrayList<>();
        for (Module m : byName.values()) if (m.category == category) out.add(m);
        return out;
    }
}
