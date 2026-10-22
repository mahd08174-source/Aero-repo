package com.aero.client.core.settings;

import java.util.function.Consumer;

public class DoubleSetting extends Setting<Double> {
    public final double min;
    public final double max;
    public final double sliderMin;
    public final double sliderMax;

    private DoubleSetting(String name, String description, double defaultValue, double min, double max, double sliderMin, double sliderMax, Consumer<Double> onChanged) {
        super(name, description, defaultValue, onChanged);
        this.min = min;
        this.max = max;
        this.sliderMin = sliderMin;
        this.sliderMax = sliderMax;
    }

    @Override
    public void set(Double v) { super.set(Math.max(min, Math.min(max, v))); }

    @Override
    public String serialize() { return Double.toString(get()); }

    @Override
    public boolean deserialize(String text) {
        try {
            set(Double.parseDouble(text.trim()));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static class Builder {
        private String name = "unnamed";
        private String description = "";
        private double defaultValue;
        private double min = -Double.MAX_VALUE;
        private double max = Double.MAX_VALUE;
        private double sliderMin;
        private double sliderMax = 10;
        private boolean sliderMinSet;
        private boolean sliderMaxSet;
        private Consumer<Double> onChanged;

        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder defaultValue(double defaultValue) { this.defaultValue = defaultValue; return this; }
        public Builder min(double min) { this.min = min; return this; }
        public Builder max(double max) { this.max = max; return this; }
        public Builder range(double min, double max) { this.min = min; this.max = max; return this; }
        public Builder sliderMin(double sliderMin) { this.sliderMin = sliderMin; this.sliderMinSet = true; return this; }
        public Builder sliderMax(double sliderMax) { this.sliderMax = sliderMax; this.sliderMaxSet = true; return this; }
        public Builder sliderRange(double sliderMin, double sliderMax) { return sliderMin(sliderMin).sliderMax(sliderMax); }
        public Builder onChanged(Consumer<Double> onChanged) { this.onChanged = onChanged; return this; }

        public DoubleSetting build() {
            double sMin = sliderMinSet ? sliderMin : (min != -Double.MAX_VALUE ? min : Math.min(0, defaultValue));
            double sMax = sliderMaxSet ? sliderMax : (max != Double.MAX_VALUE ? max : Math.max(10, defaultValue * 2));
            return new DoubleSetting(name, description, defaultValue, min, max, sMin, Math.max(sMin + 0.001, sMax), onChanged);
        }
    }
}
