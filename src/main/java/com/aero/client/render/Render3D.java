package com.aero.client.render;

import com.aero.client.api.EventBus;
import com.aero.client.api.Render3DEvent;
import com.aero.client.api.Renderer3D;
import com.aero.client.mixin.WorldRendererAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.world.debug.gizmo.GizmoDrawing;

/** Called once per frame right after the camera updates (see CameraMixin). */
public final class Render3D {
    private static final Renderer3D RENDERER = new Renderer3D();

    public static void render(Camera camera) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null || mc.worldRenderer == null) return;
        var collector = ((WorldRendererAccessor) mc.worldRenderer).aero$getGizmoCollector();
        try (var scope = GizmoDrawing.using(collector)) {
            EventBus.post(new Render3DEvent(RENDERER, 0));
        }
    }
    private Render3D() {}
}
