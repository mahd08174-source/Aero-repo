package com.aero.client.core.settings;

import java.util.function.Consumer;

public class StringSetting extends Setting<String> {
    private StringSetting(String name, String description, String defaultValue, Consumer<String> onChanged) {
        super(name, description, defaultValue, onChanged);
    }

    @Override
    public String serialize() { return get(); }

    @Override
    public boolean deserialize(String text) {
        set(text);
        return true;
    }

    public static class Builder {
        private String name = "unnamed";
        private String description = "";
        private String defaultValue = "";
        private Consumer<String> onChanged;

        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder defaultValue(String defaultValue) { this.defaultValue = defaultValue; return this; }
        public Builder onChanged(Consumer<String> onChanged) { this.onChanged = onChanged; return this; }
        public StringSetting build() { return new StringSetting(name, description, defaultValue, onChanged); }
    }
}
