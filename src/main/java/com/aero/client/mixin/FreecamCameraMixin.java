package com.aero.client.mixin;

import com.aero.client.core.render.RenderHooks;
import com.aero.client.modules.CustomFreecam;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_243;
import net.minecraft.class_4184;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_4184.class})
public abstract class FreecamCameraMixin {
    @Shadow
    private class_243 field_18712;

    @Shadow
    protected abstract void method_19327(double var1, double var3, double var5);

    @Shadow
    protected abstract void method_19325(float var1, float var2);

    @Inject(method={"method_19321"}, at={@At(value="TAIL")})
    private void onUpdate(class_1937 area, class_1297 focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        CustomFreecam fc = CustomFreecam.INSTANCE;
        if (fc != null && fc.isActive()) {
            this.method_19327(fc.getInterpolatedX(tickDelta), fc.getInterpolatedY(tickDelta), fc.getInterpolatedZ(tickDelta));
            this.method_19325(fc.getInterpolatedYaw(tickDelta), fc.getInterpolatedPitch(tickDelta));
        }
        class_4184 self = (class_4184) (Object) this;
        RenderHooks.onCameraUpdate(this.field_18712, self.method_19330(), self.method_19329(), tickDelta);
    }
}
