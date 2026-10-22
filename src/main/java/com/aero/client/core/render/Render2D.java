package com.aero.client.core.render;

import net.minecraft.class_332;

/** Holds the draw context of the HUD frame that is currently being rendered. */
public final class Render2D {
    private static class_332 context;

    private Render2D() {}

    public static void set(class_332 ctx) { context = ctx; }

    public static class_332 context() { return context; }
}
