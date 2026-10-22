package com.aero.client.mixin;

import net.minecraft.client.render.WorldRenderer;
import net.minecraft.world.debug.gizmo.GizmoCollectorImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WorldRenderer.class)
public interface WorldRendererAccessor {
    @Accessor("gizmoCollector")
    GizmoCollectorImpl aero$getGizmoCollector();
}
