package com.aero.client.core.settings;

import java.util.function.Consumer;

/** A named, typed value owned by a module. */
public abstract class Setting<T> {
    public final String name;
    public final String description;
    protected T value;
    protected final T defaultValue;
    private final Consumer<T> onChanged;

    protected Setting(String name, String description, T defaultValue, Consumer<T> onChanged) {
        this.name = name;
        this.description = description == null ? "" : description;
        this.defaultValue = defaultValue;
        this.value = copyOf(defaultValue);
        this.onChanged = onChanged;
    }

    /** Hook for mutable value types (colors) so the default is never edited in place. */
    protected T copyOf(T v) { return v; }

    public T get() { return value; }

    public void set(T v) {
        this.value = v;
        if (onChanged != null) onChanged.accept(v);
    }

    public void reset() { set(copyOf(defaultValue)); }

    /** Value as text for the config file. */
    public abstract String serialize();

    /** Applies a value from the config file; returns false when the text is invalid. */
    public abstract boolean deserialize(String text);
}
