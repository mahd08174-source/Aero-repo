package com.aero.client.modules;

import com.aero.client.AeroCategory;
import com.aero.client.api.TickEvent;
import com.aero.client.api.BoolSetting;
import com.aero.client.api.DoubleSetting;
import com.aero.client.api.Setting;
import com.aero.client.api.SettingGroup;
import com.aero.client.api.Module;
import com.aero.client.api.EventHandler;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
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
        if (this.mc.player == null) {
            this.toggle();
            return;
        }
        this.savedPlayerYaw = this.mc.player.getYaw();
        this.savedPlayerPitch = this.mc.player.getPitch();
        this.wasSneaking = this.mc.player.isSneaking();
        this.yaw = this.savedPlayerYaw;
        this.pitch = this.savedPlayerPitch;
        this.prevYaw = this.yaw;
        this.prevPitch = this.pitch;
        Vec3d cam = this.mc.player.getCameraPosVec(1.0f);
        this.currentPos.set(cam.x, cam.y, cam.z);
        this.prevPos.set(cam.x, cam.y, cam.z);
        this.velocity.set(0.0, 0.0, 0.0);
        this.lastTickTime = System.currentTimeMillis();
        this.mc.chunkCullingEnabled = false;
    }

    public void onDeactivate() {
        if (this.mc.player != null) {
            this.mc.player.setYaw(this.savedPlayerYaw);
            this.mc.player.setPitch(this.savedPlayerPitch);
            this.mc.player.setHeadYaw(this.savedPlayerYaw);
            this.mc.player.setBodyYaw(this.savedPlayerYaw);
            this.mc.player.setSneaking(this.wasSneaking);
        }
        this.mc.chunkCullingEnabled = true;
        this.velocity.set(0.0, 0.0, 0.0);
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.mc.player == null) {
            return;
        }
        float yawDelta = this.mc.player.getYaw() - this.savedPlayerYaw;
        float pitchDelta = this.mc.player.getPitch() - this.savedPlayerPitch;
        this.yaw = MathHelper.wrapDegrees((float)(this.yaw + yawDelta));
        this.pitch = MathHelper.clamp((float)(this.pitch + pitchDelta), (float)-90.0f, (float)90.0f);
        this.mc.player.setYaw(this.savedPlayerYaw);
        this.mc.player.setPitch(this.savedPlayerPitch);
        this.mc.player.setHeadYaw(this.savedPlayerYaw);
        this.mc.player.setBodyYaw(this.savedPlayerYaw);
        if (((Boolean)this.keepSneak.get()).booleanValue() && this.wasSneaking) {
            this.mc.player.setSneaking(true);
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
        if (this.mc.options.sprintKey.isPressed()) {
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
        if (this.mc.options.forwardKey.isPressed()) {
            tx += fwdX * spd;
            tz += fwdZ * spd;
        }
        if (this.mc.options.backKey.isPressed()) {
            tx -= fwdX * spd;
            tz -= fwdZ * spd;
        }
        if (this.mc.options.rightKey.isPressed()) {
            tx += rightX * spd;
            tz += rightZ * spd;
        }
        if (this.mc.options.leftKey.isPressed()) {
            tx -= rightX * spd;
            tz -= rightZ * spd;
        }
        if (this.mc.options.jumpKey.isPressed()) {
            ty += spd;
        }
        if (this.mc.options.sneakKey.isPressed()) {
            ty -= spd;
        }
        double smooth = 5.0;
        if (((Boolean)this.smoothing.get()).booleanValue()) {
            double lerpF = 1.0 - Math.pow(0.001, dt);
            this.velocity.x = MathHelper.lerp((double)lerpF, (double)this.velocity.x, (double)(tx * smooth));
            this.velocity.y = MathHelper.lerp((double)lerpF, (double)this.velocity.y, (double)(ty * smooth));
            this.velocity.z = MathHelper.lerp((double)lerpF, (double)this.velocity.z, (double)(tz * smooth));
        } else {
            this.velocity.set(tx * smooth, ty * smooth, tz * smooth);
        }
        this.currentPos.x += this.velocity.x * (double)dt;
        this.currentPos.y += this.velocity.y * (double)dt;
        this.currentPos.z += this.velocity.z * (double)dt;
    }

    public double getInterpolatedX(float td) {
        return MathHelper.lerp((double)td, (double)this.prevPos.x, (double)this.currentPos.x);
    }

    public double getInterpolatedY(float td) {
        return MathHelper.lerp((double)td, (double)this.prevPos.y, (double)this.currentPos.y);
    }

    public double getInterpolatedZ(float td) {
        return MathHelper.lerp((double)td, (double)this.prevPos.z, (double)this.currentPos.z);
    }

    public float getInterpolatedYaw(float td) {
        return MathHelper.lerp((float)td, (float)this.prevYaw, (float)this.yaw);
    }

    public float getInterpolatedPitch(float td) {
        return MathHelper.lerp((float)td, (float)this.prevPitch, (float)this.pitch);
    }
}

