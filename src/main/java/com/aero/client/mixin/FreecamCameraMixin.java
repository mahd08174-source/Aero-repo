package com.aero.client.mixin;

import com.aero.client.modules.CustomFreecam;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Camera.class})
public abstract class FreecamCameraMixin {
    @Shadow
    protected abstract void setPos(double var1, double var3, double var5);

    @Shadow
    protected abstract void setRotation(float var1, float var2);

    @Inject(method={"update"}, at={@At(value="TAIL")})
    private void onUpdate(World area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        CustomFreecam fc = CustomFreecam.INSTANCE;
        if (fc != null && fc.isActive()) {
            this.setPos(fc.getInterpolatedX(tickDelta), fc.getInterpolatedY(tickDelta), fc.getInterpolatedZ(tickDelta));
            this.setRotation(fc.getInterpolatedYaw(tickDelta), fc.getInterpolatedPitch(tickDelta));
        }
    }
}

