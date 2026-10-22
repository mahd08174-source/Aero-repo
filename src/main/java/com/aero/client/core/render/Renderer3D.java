package com.aero.client.core.render;

import net.minecraft.class_12179;
import net.minecraft.class_12180;
import net.minecraft.class_238;
import net.minecraft.class_243;

/**
 * Draws world-space boxes and lines through Minecraft's own gizmo system, so no custom render pipeline is needed.
 * Everything is drawn through walls (occlusion ignored), like an ESP.
 * Only valid while a {@link com.aero.client.core.events.Render3DEvent} is being handled.
 */
public final class Renderer3D {
    public static final Renderer3D INSTANCE = new Renderer3D();

    private Renderer3D() {}

    /** Draws a box. {@code excludeDir} is accepted for source compatibility and ignored. */
    public void box(double x1, double y1, double z1, double x2, double y2, double z2,
                    Color sideColor, Color lineColor, ShapeMode mode, int excludeDir) {
        class_238 box = new class_238(x1, y1, z1, x2, y2, z2);
        if ((mode == ShapeMode.Sides || mode == ShapeMode.Both) && sideColor != null && sideColor.a > 0) {
            class_12180.method_75541(box, class_12179.method_75539(sideColor.getPacked())).method_75533();
        }
        if ((mode == ShapeMode.Lines || mode == ShapeMode.Both) && lineColor != null && lineColor.a > 0) {
            class_12180.method_75541(box, class_12179.method_75535(lineColor.getPacked())).method_75533();
        }
    }

    public void line(double x1, double y1, double z1, double x2, double y2, double z2, Color color) {
        if (color == null || color.a <= 0) return;
        class_12180.method_75545(new class_243(x1, y1, z1), new class_243(x2, y2, z2), color.getPacked()).method_75533();
    }
}
