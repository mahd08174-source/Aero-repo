package com.aero.client.mixin;

import com.aero.client.render.Render3D;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Camera.class, priority = 1500)
public abstract class CameraMixin {
    @Inject(method = "update", at = @At("RETURN"))
    private void aero$render3d(CallbackInfo ci) {
        Render3D.render((Camera) (Object) this);
    }
}
