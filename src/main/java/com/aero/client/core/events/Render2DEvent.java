package com.aero.client.core.events;

import net.minecraft.class_332;

/** Fired once per frame while the HUD is drawn. */
public final class Render2DEvent {
    public final class_332 drawContext;
    public final int screenWidth;
    public final int screenHeight;
    public final double frameTime;
    public final float tickDelta;

    public Render2DEvent(class_332 drawContext, int screenWidth, int screenHeight, double frameTime, float tickDelta) {
        this.drawContext = drawContext;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.frameTime = frameTime;
        this.tickDelta = tickDelta;
    }
}
