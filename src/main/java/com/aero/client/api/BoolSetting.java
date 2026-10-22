package com.aero.client.api;

public class BoolSetting extends Setting<Boolean> {
    private BoolSetting(String n, String d, boolean def) { super(n, d, def); }
    @Override public Object save() { return value; }
    @Override public void load(Object o) { if (o instanceof Boolean b) value = b; }
    @Override public String display() { return value ? "ON" : "OFF"; }
    @Override public boolean parse(String s) {
        if (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("on")) { set(true); return true; }
        if (s.equalsIgnoreCase("false") || s.equalsIgnoreCase("off")) { set(false); return true; }
        return false;
    }
    public static class Builder {
        private String name = "", desc = ""; private boolean def;
        public Builder name(String n) { name = n; return this; }
        public Builder description(String d) { desc = d; return this; }
        public Builder defaultValue(boolean v) { def = v; return this; }
        public Builder visible(java.util.function.Supplier<Boolean> s) { return this; }
        public Builder onChanged(java.util.function.Consumer<Boolean> c) { return this; }
        public BoolSetting build() { return new BoolSetting(name, desc, def); }
    }
}
