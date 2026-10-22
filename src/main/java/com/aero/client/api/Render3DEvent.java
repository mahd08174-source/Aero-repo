package com.aero.client.api;

public class Render3DEvent {
    public final Renderer3D renderer;
    public final double tickDelta;
    public Render3DEvent(Renderer3D r, double tickDelta) { this.renderer = r; this.tickDelta = tickDelta; }
}
