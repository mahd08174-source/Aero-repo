package com.aero.client.core.render;

import net.minecraft.class_1041;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.joml.Vector3d;

/** Projects world positions to the screen and positions text there (HUD pass only). */
public final class NametagUtils {
    private static boolean began;

    private NametagUtils() {}

    /**
     * Converts {@code pos} (world coordinates) to screen coordinates in place.
     * Returns false when the point is behind the camera or far outside the screen.
     */
    public static boolean to2D(Vector3d pos, double scale) {
        if (!CameraState.valid) return false;
        class_310 mc = class_310.method_1551();
        class_1041 win = mc.method_22683();
        double dx = pos.x - CameraState.x;
        double dy = pos.y - CameraState.y;
        double dz = pos.z - CameraState.z;
        double yaw = Math.toRadians(CameraState.yaw);
        double pitch = Math.toRadians(CameraState.pitch);
        double cp = Math.cos(pitch);
        double fx = -Math.sin(yaw) * cp;
        double fy = -Math.sin(pitch);
        double fz = Math.cos(yaw) * cp;
        double lx = Math.cos(yaw);
        double lz = Math.sin(yaw);
        double ux = fy * lz;
        double uy = fz * lx - fx * lz;
        double uz = -fy * lx;

        double depth = dx * fx + dy * fy + dz * fz;
        if (depth <= 0.05) return false;
        double sx = dx * lx + dz * lz;
        double sy = dx * ux + dy * uy + dz * uz;

        double fov = mc.field_1690.method_41808().method_41753();
        double tan = Math.tan(Math.toRadians(fov) / 2.0);
        double aspect = (double) win.method_4489() / (double) Math.max(1, win.method_4506());
        double ndcX = -sx / (depth * tan * aspect);
        double ndcY = sy / (depth * tan);
        if (Math.abs(ndcX) > 1.3 || Math.abs(ndcY) > 1.3) return false;

        pos.x = (ndcX * 0.5 + 0.5) * win.method_4486();
        pos.y = (0.5 - ndcY * 0.5) * win.method_4502();
        pos.z = 0;
        return true;
    }

    /** Moves the draw origin to a screen position obtained from {@link #to2D}. */
    public static void begin(Vector3d screenPos) {
        class_332 ctx = Render2D.context();
        if (ctx == null) return;
        ctx.method_51448().pushMatrix();
        ctx.method_51448().translate((float) screenPos.x, (float) screenPos.y);
        began = true;
    }

    public static void end() {
        class_332 ctx = Render2D.context();
        if (ctx == null || !began) return;
        ctx.method_51448().popMatrix();
        began = false;
    }
}
