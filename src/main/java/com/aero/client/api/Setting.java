package com.aero.client.api;

public abstract class Setting<T> {
    public final String name, description;
    protected T value;
    public final T defaultValue;

    protected Setting(String name, String description, T def) {
        this.name = name; this.description = description == null ? "" : description;
        this.defaultValue = def; this.value = def;
    }
    public T get() { return value; }
    public void set(T v) { this.value = v; Config.markDirty(); }
    public void reset() { set(defaultValue); }

    public abstract Object save();
    public abstract void load(Object o);
    public abstract String display();
    public abstract boolean parse(String s);
}
