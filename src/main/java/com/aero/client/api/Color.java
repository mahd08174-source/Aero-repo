package com.aero.client.api;

public class Color {
    public int r, g, b, a;
    public Color() { this(255, 255, 255, 255); }
    public Color(int r, int g, int b) { this(r, g, b, 255); }
    public Color(int r, int g, int b, int a) { set(r, g, b, a); }
    public Color(Color o) { this(o.r, o.g, o.b, o.a); }

    public Color set(int r, int g, int b, int a) {
        this.r = clamp(r); this.g = clamp(g); this.b = clamp(b); this.a = clamp(a);
        return this;
    }
    public Color set(Color o) { return set(o.r, o.g, o.b, o.a); }
    /** ARGB. */
    public int getPacked() { return (a << 24) | (r << 16) | (g << 8) | b; }
    public Color a(int a) { this.a = clamp(a); return this; }
    private static int clamp(int v) { return Math.max(0, Math.min(255, v)); }

    public static int HSBtoRGB(float hue, float saturation, float brightness) {
        return java.awt.Color.HSBtoRGB(hue, saturation, brightness);
    }
}
