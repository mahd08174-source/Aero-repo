package com.aero.client.api;

public class ColorSetting extends Setting<SettingColor> {
    private ColorSetting(String n, String d, SettingColor def) { super(n, d, def); value = new SettingColor(def); }
    @Override public Object save() { return java.util.List.of(value.r, value.g, value.b, value.a); }
    @Override public void load(Object o) {
        if (o instanceof java.util.List<?> l && l.size() == 4)
            value = new SettingColor(((Number) l.get(0)).intValue(), ((Number) l.get(1)).intValue(), ((Number) l.get(2)).intValue(), ((Number) l.get(3)).intValue());
    }
    @Override public String display() { return String.format("#%02X%02X%02X", value.r, value.g, value.b); }
    @Override public boolean parse(String s) {
        try {
            s = s.trim().replace("#", "");
            int rgb = Integer.parseInt(s.substring(0, 6), 16);
            int a = s.length() >= 8 ? Integer.parseInt(s.substring(6, 8), 16) : value.a;
            set(new SettingColor((rgb >> 16) & 255, (rgb >> 8) & 255, rgb & 255, a));
            return true;
        } catch (Exception e) { return false; }
    }
    public static class Builder {
        private String name = "", desc = ""; private SettingColor def = new SettingColor(255, 255, 255, 255);
        public Builder name(String n) { name = n; return this; }
        public Builder description(String d) { desc = d; return this; }
        public Builder defaultValue(Color c) { def = new SettingColor(c); return this; }
        public Builder visible(java.util.function.Supplier<Boolean> s) { return this; }
        public ColorSetting build() { return new ColorSetting(name, desc, def); }
    }
}
