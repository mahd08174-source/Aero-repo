package com.aero.client.api;

public class IntSetting extends Setting<Integer> {
    public final int min, max, sliderMin, sliderMax;
    private IntSetting(String n, String d, int def, int min, int max, int smin, int smax) {
        super(n, d, def); this.min = min; this.max = max; this.sliderMin = smin; this.sliderMax = smax;
    }
    @Override public void set(Integer v) { super.set(Math.max(min, Math.min(max, v))); }
    @Override public Object save() { return value; }
    @Override public void load(Object o) { if (o instanceof Number n) value = Math.max(min, Math.min(max, n.intValue())); }
    @Override public String display() { return String.valueOf(value); }
    @Override public boolean parse(String s) { try { set(Integer.parseInt(s.trim())); return true; } catch (Exception e) { return false; } }
    public static class Builder {
        private String name = "", desc = ""; private int def, min = Integer.MIN_VALUE, max = Integer.MAX_VALUE, smin = Integer.MIN_VALUE, smax = Integer.MIN_VALUE;
        public Builder name(String n) { name = n; return this; }
        public Builder description(String d) { desc = d; return this; }
        public Builder defaultValue(int v) { def = v; return this; }
        public Builder min(int v) { min = v; return this; }
        public Builder max(int v) { max = v; return this; }
        public Builder range(int a, int b) { min = a; max = b; return this; }
        public Builder sliderMin(int v) { smin = v; return this; }
        public Builder sliderMax(int v) { smax = v; return this; }
        public Builder sliderRange(int a, int b) { smin = a; smax = b; return this; }
        public Builder visible(java.util.function.Supplier<Boolean> s) { return this; }
        public Builder onChanged(java.util.function.Consumer<Integer> c) { return this; }
        public IntSetting build() {
            int lo = smin != Integer.MIN_VALUE ? smin : (min != Integer.MIN_VALUE ? min : 0);
            int hi = smax != Integer.MIN_VALUE ? smax : (max != Integer.MAX_VALUE ? max : Math.max(100, def * 2));
            return new IntSetting(name, desc, def, min, max, lo, hi);
        }
    }
}
