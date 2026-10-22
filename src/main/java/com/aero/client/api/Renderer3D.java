package com.aero.client.api;

import net.minecraft.client.render.DrawStyle;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.debug.gizmo.GizmoDrawing;

/** World-space box / line drawing through Minecraft's gizmo system (valid only inside Render3D). */
public class Renderer3D {
    public float lineWidth = 2.0f;
    public boolean throughWalls = true;

    public void box(double x1, double y1, double z1, double x2, double y2, double z2,
                    Color side, Color line, ShapeMode mode, int excludeDir) {
        Box b = new Box(x1, y1, z1, x2, y2, z2);
        if (mode != ShapeMode.Lines && side != null && side.a > 0) {
            var g = GizmoDrawing.box(b, DrawStyle.filled(side.getPacked()));
            if (throughWalls) g.ignoreOcclusion();
        }
        if (mode != ShapeMode.Sides && line != null && line.a > 0) {
            var g = GizmoDrawing.box(b, DrawStyle.stroked(line.getPacked(), lineWidth));
            if (throughWalls) g.ignoreOcclusion();
        }
    }

    public void box(Box b, Color side, Color line, ShapeMode mode, int excludeDir) {
        box(b.minX, b.minY, b.minZ, b.maxX, b.maxY, b.maxZ, side, line, mode, excludeDir);
    }

    public void line(double x1, double y1, double z1, double x2, double y2, double z2, Color color) {
        if (color == null || color.a <= 0) return;
        var g = GizmoDrawing.line(new Vec3d(x1, y1, z1), new Vec3d(x2, y2, z2), color.getPacked());
        if (throughWalls) g.ignoreOcclusion();
    }
}
