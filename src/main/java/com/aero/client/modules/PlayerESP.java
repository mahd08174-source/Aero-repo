package com.aero.client.modules;

import com.aero.client.AeroCategory;
import com.aero.client.api.*;
import com.aero.client.api.Module;
import net.minecraft.client.network.AbstractClientPlayerEntity;

public class PlayerESP extends Module {
    private final Setting<SettingColor> fill = settings.getDefaultGroup().add(new ColorSetting.Builder()
        .name("fill-color").description("Box fill color.").defaultValue(new SettingColor(111, 211, 255, 40)).build());
    private final Setting<SettingColor> line = settings.getDefaultGroup().add(new ColorSetting.Builder()
        .name("line-color").description("Box outline color.").defaultValue(new SettingColor(111, 211, 255, 255)).build());
    private final Setting<Boolean> tracers = settings.getDefaultGroup().add(new BoolSetting.Builder()
        .name("tracers").description("Draw a line from your crosshair to each player.").defaultValue(false).build());

    public PlayerESP() { super(AeroCategory.HUD_V1, "player-esp", "Draws boxes around other players."); }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mc.world == null || mc.player == null) return;
        var cam = mc.gameRenderer.getCamera().getCameraPos();
        for (AbstractClientPlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player) continue;
            event.renderer.box(p.getBoundingBox(), fill.get(), line.get(), ShapeMode.Both, 0);
            if (tracers.get()) {
                var c = p.getBoundingBox().getCenter();
                event.renderer.line(cam.x, cam.y, cam.z, c.x, c.y, c.z, line.get());
            }
        }
    }
}
