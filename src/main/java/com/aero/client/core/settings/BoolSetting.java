package com.aero.client.core.settings;

import java.util.function.Consumer;

public class BoolSetting extends Setting<Boolean> {
    private BoolSetting(String name, String description, boolean defaultValue, Consumer<Boolean> onChanged) {
        super(name, description, defaultValue, onChanged);
    }

    @Override
    public String serialize() { return Boolean.toString(get()); }

    @Override
    public boolean deserialize(String text) {
        if ("true".equalsIgnoreCase(text)) { set(true); return true; }
        if ("false".equalsIgnoreCase(text)) { set(false); return true; }
        return false;
    }

    public static class Builder {
        private String name = "unnamed";
        private String description = "";
        private boolean defaultValue;
        private Consumer<Boolean> onChanged;

        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder defaultValue(boolean defaultValue) { this.defaultValue = defaultValue; return this; }
        public Builder onChanged(Consumer<Boolean> onChanged) { this.onChanged = onChanged; return this; }
        public BoolSetting build() { return new BoolSetting(name, description, defaultValue, onChanged); }
    }
}
