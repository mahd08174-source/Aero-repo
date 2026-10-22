package com.aero.client.core.events;

import com.aero.client.core.render.Renderer3D;

/** Fired once per frame while the world is rendered; draw boxes and lines through {@link #renderer}. */
public final class Render3DEvent {
    public final Renderer3D renderer;
    public final float tickDelta;

    public Render3DEvent(Renderer3D renderer, float tickDelta) {
        this.renderer = renderer;
        this.tickDelta = tickDelta;
    }
}
