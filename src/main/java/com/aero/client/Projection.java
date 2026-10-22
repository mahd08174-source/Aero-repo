package com.aero.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;

/** World -> scaled-GUI-pixel projection from the camera's position, yaw/pitch and the FOV option. */
public final class Projection {
    /** @return {x, y} in scaled GUI pixels, or null when behind the camera. */
    public static double[] toScreen(double wx, double wy, double wz) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return null;
        Camera cam = mc.gameRenderer.getCamera();
        Vec3d c = cam.getCameraPos();
        double yaw = Math.toRadians(cam.getYaw()), pitch = Math.toRadians(cam.getPitch());
        double cp = Math.cos(pitch);
        double fx = -Math.sin(yaw) * cp, fy = -Math.sin(pitch), fz = Math.cos(yaw) * cp;
        double rx = -Math.cos(yaw), rz = -Math.sin(yaw);
        double upx = -rz * fy, upy = rz * fx - rx * fz, upz = rx * fy;
        double dx = wx - c.x, dy = wy - c.y, dz = wz - c.z;
        double zc = dx * fx + dy * fy + dz * fz;
        if (zc < 0.05) return null;
        double xc = dx * rx + dz * rz;
        double yc = dx * upx + dy * upy + dz * upz;
        int w = mc.getWindow().getScaledWidth(), h = mc.getWindow().getScaledHeight();
        double tanHalf = Math.tan(Math.toRadians(mc.options.getFov().getValue()) / 2.0);
        double aspect = (double) w / (double) h;
        double ndcX = xc / (zc * tanHalf * aspect), ndcY = yc / (zc * tanHalf);
        return new double[]{(ndcX * 0.5 + 0.5) * w, (0.5 - ndcY * 0.5) * h};
    }
    private Projection() {}
}
