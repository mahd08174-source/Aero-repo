package com.aero.client.api;

public class EnumSetting<T extends Enum<T>> extends Setting<T> {
    private EnumSetting(String n, String d, T def) { super(n, d, def); }
    @Override public Object save() { return value.name(); }
    @Override @SuppressWarnings("unchecked") public void load(Object o) {
        if (!(o instanceof String s)) return;
        for (Object c : value.getDeclaringClass().getEnumConstants()) if (((Enum<?>) c).name().equals(s)) value = (T) c;
    }
    @Override public String display() { return value.name(); }
    @SuppressWarnings("unchecked") public void cycle() {
        Object[] all = value.getDeclaringClass().getEnumConstants();
        set((T) all[(value.ordinal() + 1) % all.length]);
    }
    @Override @SuppressWarnings("unchecked") public boolean parse(String s) {
        for (Object c : value.getDeclaringClass().getEnumConstants())
            if (((Enum<?>) c).name().equalsIgnoreCase(s.trim())) { set((T) c); return true; }
        return false;
    }
    public static class Builder<T extends Enum<T>> {
        private String name = "", desc = ""; private T def;
        public Builder<T> name(String n) { name = n; return this; }
        public Builder<T> description(String d) { desc = d; return this; }
        public Builder<T> defaultValue(T v) { def = v; return this; }
        public Builder<T> visible(java.util.function.Supplier<Boolean> s) { return this; }
        public EnumSetting<T> build() { return new EnumSetting<>(name, desc, def); }
    }
}
