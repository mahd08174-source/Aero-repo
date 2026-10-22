package com.aero.client.api;

public class StringSetting extends Setting<String> {
    private StringSetting(String n, String d, String def) { super(n, d, def); }
    @Override public Object save() { return value; }
    @Override public void load(Object o) { if (o instanceof String s) value = s; }
    @Override public String display() { return value.isEmpty() ? "(empty)" : value; }
    @Override public boolean parse(String s) { set(s); return true; }
    public static class Builder {
        private String name = "", desc = "", def = "";
        public Builder name(String n) { name = n; return this; }
        public Builder description(String d) { desc = d; return this; }
        public Builder defaultValue(String v) { def = v; return this; }
        public Builder visible(java.util.function.Supplier<Boolean> s) { return this; }
        public StringSetting build() { return new StringSetting(name, desc, def); }
    }
}
