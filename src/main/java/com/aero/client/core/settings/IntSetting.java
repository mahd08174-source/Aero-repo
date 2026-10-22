package com.aero.client.core.settings;

import java.util.function.Consumer;

public class IntSetting extends Setting<Integer> {
    public final int min;
    public final int max;
    public final int sliderMin;
    public final int sliderMax;

    private IntSetting(String name, String description, int defaultValue, int min, int max, int sliderMin, int sliderMax, Consumer<Integer> onChanged) {
        super(name, description, defaultValue, onChanged);
        this.min = min;
        this.max = max;
        this.sliderMin = sliderMin;
        this.sliderMax = sliderMax;
    }

    @Override
    public void set(Integer v) { super.set(Math.max(min, Math.min(max, v))); }

    @Override
    public String serialize() { return Integer.toString(get()); }

    @Override
    public boolean deserialize(String text) {
        try {
            set(Integer.parseInt(text.trim()));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static class Builder {
        private String name = "unnamed";
        private String description = "";
        private int defaultValue;
        private int min = Integer.MIN_VALUE;
        private int max = Integer.MAX_VALUE;
        private int sliderMin;
        private int sliderMax = 100;
        private boolean sliderMinSet;
        private boolean sliderMaxSet;
        private Consumer<Integer> onChanged;

        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder defaultValue(int defaultValue) { this.defaultValue = defaultValue; return this; }
        public Builder min(int min) { this.min = min; return this; }
        public Builder max(int max) { this.max = max; return this; }
        public Builder range(int min, int max) { this.min = min; this.max = max; return this; }
        public Builder sliderMin(int sliderMin) { this.sliderMin = sliderMin; this.sliderMinSet = true; return this; }
        public Builder sliderMax(int sliderMax) { this.sliderMax = sliderMax; this.sliderMaxSet = true; return this; }
        public Builder sliderRange(int sliderMin, int sliderMax) { return sliderMin(sliderMin).sliderMax(sliderMax); }
        public Builder onChanged(Consumer<Integer> onChanged) { this.onChanged = onChanged; return this; }

        public IntSetting build() {
            int sMin = sliderMinSet ? sliderMin : (min != Integer.MIN_VALUE ? min : Math.min(0, defaultValue));
            int sMax = sliderMaxSet ? sliderMax : (max != Integer.MAX_VALUE ? max : Math.max(100, defaultValue * 2));
            return new IntSetting(name, description, defaultValue, min, max, sMin, Math.max(sMin + 1, sMax), onChanged);
        }
    }
}
