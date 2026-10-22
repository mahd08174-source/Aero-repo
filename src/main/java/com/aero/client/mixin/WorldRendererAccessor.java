package com.aero.client.mixin;

import net.minecraft.class_12185;
import net.minecraft.class_761;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Gives the 3D renderer access to the world renderer's gizmo collector. */
@Mixin(class_761.class)
public interface WorldRendererAccessor {
    @Accessor("field_63574")
    class_12185 aero$getGizmoCollector();
}
