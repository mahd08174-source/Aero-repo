package com.aero.client.core.render;

import com.aero.client.core.events.Events;
import com.aero.client.core.events.Render3DEvent;
import com.aero.client.mixin.WorldRendererAccessor;
import net.minecraft.class_12180;
import net.minecraft.class_243;
import net.minecraft.class_310;

/** Called by the camera mixin once per frame, right after the camera has been updated. */
public final class RenderHooks {
    private RenderHooks() {}

    public static void onCameraUpdate(class_243 pos, float yaw, float pitch, float tickDelta) {
        if (pos != null) CameraState.set(pos.field_1352, pos.field_1351, pos.field_1350, yaw, pitch);
        class_310 mc = class_310.method_1551();
        if (mc.field_1687 == null || mc.field_1724 == null || mc.field_1769 == null) return;
        if (!Events.hasSubscribers(Render3DEvent.class)) return;
        try (var scope = class_12180.method_75554(((WorldRendererAccessor) mc.field_1769).aero$getGizmoCollector())) {
            Events.post(new Render3DEvent(Renderer3D.INSTANCE, tickDelta));
        }
    }
}
