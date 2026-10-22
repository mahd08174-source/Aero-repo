package com.aero.client.api;

import java.util.ArrayList;
import java.util.List;

public final class Modules {
    private static final Modules INSTANCE = new Modules();
    private final List<Module> modules = new ArrayList<>();
    public static Modules get() { return INSTANCE; }

    public void add(Module m) { modules.add(m); }
    public List<Module> all() { return modules; }
    public Module get(String name) {
        for (Module m : modules) if (m.name.equalsIgnoreCase(name)) return m;
        return null;
    }
    @SuppressWarnings("unchecked")
    public <T extends Module> T get(Class<T> cls) {
        for (Module m : modules) if (cls.isInstance(m)) return (T) m;
        return null;
    }
    public List<Module> getGroup(Category c) {
        List<Module> out = new ArrayList<>();
        for (Module m : modules) if (m.category == c) out.add(m);
        return out;
    }
    private Modules() {}
}
