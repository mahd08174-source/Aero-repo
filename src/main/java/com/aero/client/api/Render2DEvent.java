package com.aero.client.api;

import net.minecraft.client.gui.DrawContext;

public class Render2DEvent {
    public final DrawContext drawContext;
    public final int screenWidth, screenHeight;
    public final double tickDelta;
    public Render2DEvent(DrawContext ctx, int w, int h, double tickDelta) {
        this.drawContext = ctx; this.screenWidth = w; this.screenHeight = h; this.tickDelta = tickDelta;
    }
}
