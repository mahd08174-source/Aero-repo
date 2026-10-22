package com.aero.client.core.settings;

import com.aero.client.core.render.SettingColor;

import java.util.function.Consumer;

public class ColorSetting extends Setting<SettingColor> {
    private ColorSetting(String name, String description, SettingColor defaultValue, Consumer<SettingColor> onChanged) {
        super(name, description, defaultValue, onChanged);
    }

    @Override
    protected SettingColor copyOf(SettingColor v) { return new SettingColor(v); }

    @Override
    public String serialize() {
        SettingColor c = get();
        return c.r + "," + c.g + "," + c.b + "," + c.a;
    }

    @Override
    public boolean deserialize(String text) {
        try {
            String[] p = text.trim().split(",");
            if (p.length < 3) return false;
            int a = p.length > 3 ? Integer.parseInt(p[3].trim()) : 255;
            set(new SettingColor(Integer.parseInt(p[0].trim()), Integer.parseInt(p[1].trim()), Integer.parseInt(p[2].trim()), a));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static class Builder {
        private String name = "unnamed";
        private String description = "";
        private SettingColor defaultValue = new SettingColor(255, 255, 255, 255);
        private Consumer<SettingColor> onChanged;

        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder defaultValue(SettingColor defaultValue) { this.defaultValue = defaultValue; return this; }
        public Builder onChanged(Consumer<SettingColor> onChanged) { this.onChanged = onChanged; return this; }
        public ColorSetting build() { return new ColorSetting(name, description, defaultValue, onChanged); }
    }
}
