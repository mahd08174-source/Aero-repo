package com.aero.client.core.settings;

import java.util.function.Consumer;

public class EnumSetting<T extends Enum<?>> extends Setting<T> {
    private EnumSetting(String name, String description, T defaultValue, Consumer<T> onChanged) {
        super(name, description, defaultValue, onChanged);
    }

    @SuppressWarnings("unchecked")
    public T[] values() {
        Object[] constants = defaultValue.getDeclaringClass().getEnumConstants();
        return (T[]) constants;
    }

    /** Moves to the next (or previous) constant, wrapping around. */
    public void cycle(boolean forward) {
        T[] all = values();
        int i = get().ordinal() + (forward ? 1 : -1);
        set(all[(i % all.length + all.length) % all.length]);
    }

    @Override
    public String serialize() { return get().name(); }

    @Override
    public boolean deserialize(String text) {
        for (T t : values()) {
            if (t.name().equalsIgnoreCase(text.trim())) {
                set(t);
                return true;
            }
        }
        return false;
    }

    public static class Builder<T extends Enum<?>> {
        private String name = "unnamed";
        private String description = "";
        private T defaultValue;
        private Consumer<T> onChanged;

        public Builder<T> name(String name) { this.name = name; return this; }
        public Builder<T> description(String description) { this.description = description; return this; }
        public Builder<T> defaultValue(T defaultValue) { this.defaultValue = defaultValue; return this; }
        public Builder<T> onChanged(Consumer<T> onChanged) { this.onChanged = onChanged; return this; }
        public EnumSetting<T> build() { return new EnumSetting<>(name, description, defaultValue, onChanged); }
    }
}
