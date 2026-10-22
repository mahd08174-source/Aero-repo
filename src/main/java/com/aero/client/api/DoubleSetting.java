package com.aero.client.api;

public class DoubleSetting extends Setting<Double> {
    public final double min, max, sliderMin, sliderMax;
    private DoubleSetting(String n, String d, double def, double min, double max, double smin, double smax) {
        super(n, d, def); this.min = min; this.max = max; this.sliderMin = smin; this.sliderMax = smax;
    }
    @Override public void set(Double v) { super.set(Math.max(min, Math.min(max, v))); }
    @Override public Object save() { return value; }
    @Override public void load(Object o) { if (o instanceof Number n) value = Math.max(min, Math.min(max, n.doubleValue())); }
    @Override public String display() { return String.format("%.2f", value); }
    @Override public boolean parse(String s) { try { set(Double.parseDouble(s.trim())); return true; } catch (Exception e) { return false; } }
    public static class Builder {
        private String name = "", desc = ""; private double def, min = -Double.MAX_VALUE, max = Double.MAX_VALUE, smin = Double.NaN, smax = Double.NaN;
        public Builder name(String n) { name = n; return this; }
        public Builder description(String d) { desc = d; return this; }
        public Builder defaultValue(double v) { def = v; return this; }
        public Builder min(double v) { min = v; return this; }
        public Builder max(double v) { max = v; return this; }
        public Builder range(double a, double b) { min = a; max = b; return this; }
        public Builder sliderMin(double v) { smin = v; return this; }
        public Builder sliderMax(double v) { smax = v; return this; }
        public Builder sliderRange(double a, double b) { smin = a; smax = b; return this; }
        public Builder visible(java.util.function.Supplier<Boolean> s) { return this; }
        public Builder onChanged(java.util.function.Consumer<Double> c) { return this; }
        public DoubleSetting build() {
            double lo = !Double.isNaN(smin) ? smin : (min > -Double.MAX_VALUE ? min : 0);
            double hi = !Double.isNaN(smax) ? smax : (max < Double.MAX_VALUE ? max : Math.max(10, def * 2));
            return new DoubleSetting(name, desc, def, min, max, lo, hi);
        }
    }
}
