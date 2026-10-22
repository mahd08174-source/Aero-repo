package com.aero.client.core.render;

/** A {@link Color} that is stored in a color setting. */
public class SettingColor extends Color {
    public SettingColor() { super(); }

    public SettingColor(int r, int g, int b) { super(r, g, b); }

    public SettingColor(int r, int g, int b, int a) { super(r, g, b, a); }

    public SettingColor(Color other) { super(other); }

    @Override
    public SettingColor copy() { return new SettingColor(this); }
}
