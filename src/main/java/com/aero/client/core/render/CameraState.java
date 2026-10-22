package com.aero.client.core.render;

/** Camera position and rotation of the frame that is currently being rendered. */
public final class CameraState {
    public static volatile double x;
    public static volatile double y;
    public static volatile double z;
    public static volatile float yaw;
    public static volatile float pitch;
    public static volatile boolean valid;

    private CameraState() {}

    public static void set(double x, double y, double z, float yaw, float pitch) {
        CameraState.x = x;
        CameraState.y = y;
        CameraState.z = z;
        CameraState.yaw = yaw;
        CameraState.pitch = pitch;
        CameraState.valid = true;
    }
}
