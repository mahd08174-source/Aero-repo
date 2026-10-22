package com.aero.client.core.render;

/** Simple RGBA color (0-255 per channel). */
public class Color {
    public int r;
    public int g;
    public int b;
    public int a;

    public Color() { this(255, 255, 255, 255); }

    public Color(int r, int g, int b) { this(r, g, b, 255); }

    public Color(int r, int g, int b, int a) {
        this.r = clamp(r);
        this.g = clamp(g);
        this.b = clamp(b);
        this.a = clamp(a);
    }

    public Color(Color other) { this(other.r, other.g, other.b, other.a); }

    private static int clamp(int v) { return Math.max(0, Math.min(255, v)); }

    public Color set(int r, int g, int b, int a) {
        this.r = clamp(r);
        this.g = clamp(g);
        this.b = clamp(b);
        this.a = clamp(a);
        return this;
    }

    public Color set(Color other) { return set(other.r, other.g, other.b, other.a); }

    /** Packed ARGB, as used by Minecraft's draw calls. */
    public int getPacked() { return (a << 24) | (r << 16) | (g << 8) | b; }

    public Color copy() { return new Color(this); }
}
