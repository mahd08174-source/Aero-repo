package com.aero.client.modules;

import com.aero.client.AeroCategory;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class CustomFreecam
extends Module {
    public static CustomFreecam INSTANCE;
    private final SettingGroup sg;
    public final Setting<Double> speed;
    public final Setting<Boolean> smoothing;
    public final Setting<Boolean> keepSneak;
    public final Vector3d currentPos;
    public final Vector3d prevPos;
    public final Vector3d velocity;
    public float yaw;
    public float pitch;
    public float prevYaw;
    public float prevPitch;
    private float savedPlayerYaw;
    private float savedPlayerPitch;
    private boolean wasSneaking;
    private long lastTickTime;

    public CustomFreecam() {
        super(AeroCategory.MISC_V2, "custom-freecam", "Detaches the camera from the player.");
        this.sg = this.settings.getDefaultGroup();
        this.speed = this.sg.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("speed")).description("Camera movement speed.")).defaultValue(1.0).min(0.1).max(10.0).sliderRange(0.1, 10.0).build());
        this.smoothing = this.sg.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("smoothing")).description("Smooth camera movement.")).defaultValue(true)).build());
        this.keepSneak = this.sg.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("keep-sneak")).description("Keep sneaking while freecam is active.")).defaultValue(false)).build());
        this.currentPos = new Vector3d();
        this.prevPos = new Vector3d();
        this.velocity = new Vector3d();
        INSTANCE = this;
    }

    public void onActivate() {
        if (this.mc.field_1724 == null) {
            this.toggle();
            return;
        }
        this.savedPlayerYaw = this.mc.field_1724.method_36454();
        this.savedPlayerPitch = this.mc.field_1724.method_36455();
        this.wasSneaking = this.mc.field_1724.method_5715();
        this.yaw = this.savedPlayerYaw;
        this.pitch = this.savedPlayerPitch;
        this.prevYaw = this.yaw;
        this.prevPitch = this.pitch;
        class_243 cam = this.mc.field_1724.method_5836(1.0f);
        this.currentPos.set(cam.field_1352, cam.field_1351, cam.field_1350);
        this.prevPos.set(cam.field_1352, cam.field_1351, cam.field_1350);
        this.velocity.set(0.0, 0.0, 0.0);
        this.lastTickTime = System.currentTimeMillis();
        this.mc.field_1730 = false;
    }

    public void onDeactivate() {
        if (this.mc.field_1724 != null) {
            this.mc.field_1724.method_36456(this.savedPlayerYaw);
            this.mc.field_1724.method_36457(this.savedPlayerPitch);
            this.mc.field_1724.method_5847(this.savedPlayerYaw);
            this.mc.field_1724.method_5636(this.savedPlayerYaw);
            this.mc.field_1724.method_5660(this.wasSneaking);
        }
        this.mc.field_1730 = true;
        this.velocity.set(0.0, 0.0, 0.0);
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.mc.field_1724 == null) {
            return;
        }
        float yawDelta = this.mc.field_1724.method_36454() - this.savedPlayerYaw;
        float pitchDelta = this.mc.field_1724.method_36455() - this.savedPlayerPitch;
        this.yaw = class_3532.method_15393((float)(this.yaw + yawDelta));
        this.pitch = class_3532.method_15363((float)(this.pitch + pitchDelta), (float)-90.0f, (float)90.0f);
        this.mc.field_1724.method_36456(this.savedPlayerYaw);
        this.mc.field_1724.method_36457(this.savedPlayerPitch);
        this.mc.field_1724.method_5847(this.savedPlayerYaw);
        this.mc.field_1724.method_5636(this.savedPlayerYaw);
        if (((Boolean)this.keepSneak.get()).booleanValue() && this.wasSneaking) {
            this.mc.field_1724.method_5660(true);
        }
        this.prevPos.set((Vector3dc)this.currentPos);
        this.prevYaw = this.yaw;
        this.prevPitch = this.pitch;
        long now = System.currentTimeMillis();
        float dt = Math.min(0.1f, (float)(now - this.lastTickTime) / 1000.0f);
        if (dt < 0.001f) {
            dt = 0.05f;
        }
        this.lastTickTime = now;
        double spd = (Double)this.speed.get() * 2.0;
        if (this.mc.field_1690.field_1867.method_1434()) {
            spd *= 2.0;
        }
        float yawRad = (float)Math.toRadians(this.yaw);
        double fwdX = -Math.sin(yawRad);
        double fwdZ = Math.cos(yawRad);
        double rightX = Math.cos(yawRad);
        double rightZ = Math.sin(yawRad);
        double tx = 0.0;
        double ty = 0.0;
        double tz = 0.0;
        if (this.mc.field_1690.field_1894.method_1434()) {
            tx += fwdX * spd;
            tz += fwdZ * spd;
        }
        if (this.mc.field_1690.field_1881.method_1434()) {
            tx -= fwdX * spd;
            tz -= fwdZ * spd;
        }
        if (this.mc.field_1690.field_1849.method_1434()) {
            tx += rightX * spd;
            tz += rightZ * spd;
        }
        if (this.mc.field_1690.field_1913.method_1434()) {
            tx -= rightX * spd;
            tz -= rightZ * spd;
        }
        if (this.mc.field_1690.field_1903.method_1434()) {
            ty += spd;
        }
        if (this.mc.field_1690.field_1832.method_1434()) {
            ty -= spd;
        }
        double smooth = 5.0;
        if (((Boolean)this.smoothing.get()).booleanValue()) {
            double lerpF = 1.0 - Math.pow(0.001, dt);
            this.velocity.x = class_3532.method_16436((double)lerpF, (double)this.velocity.x, (double)(tx * smooth));
            this.velocity.y = class_3532.method_16436((double)lerpF, (double)this.velocity.y, (double)(ty * smooth));
            this.velocity.z = class_3532.method_16436((double)lerpF, (double)this.velocity.z, (double)(tz * smooth));
        } else {
            this.velocity.set(tx * smooth, ty * smooth, tz * smooth);
        }
        this.currentPos.x += this.velocity.x * (double)dt;
        this.currentPos.y += this.velocity.y * (double)dt;
        this.currentPos.z += this.velocity.z * (double)dt;
    }

    public double getInterpolatedX(float td) {
        return class_3532.method_16436((double)td, (double)this.prevPos.x, (double)this.currentPos.x);
    }

    public double getInterpolatedY(float td) {
        return class_3532.method_16436((double)td, (double)this.prevPos.y, (double)this.currentPos.y);
    }

    public double getInterpolatedZ(float td) {
        return class_3532.method_16436((double)td, (double)this.prevPos.z, (double)this.currentPos.z);
    }

    public float getInterpolatedYaw(float td) {
        return class_3532.method_16439((float)td, (float)this.prevYaw, (float)this.yaw);
    }

    public float getInterpolatedPitch(float td) {
        return class_3532.method_16439((float)td, (float)this.prevPitch, (float)this.pitch);
    }
}

