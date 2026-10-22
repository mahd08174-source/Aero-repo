package com.aero.client.modules;

import com.aero.client.AeroCategory;
import com.aero.client.api.*;
import com.aero.client.api.Module;
import com.aero.client.AeroText;
import net.minecraft.client.gui.DrawContext;

/** Local custom sidebar overlay (cosmetic HUD). Lines are separated by '|'; vars: {name} {fps} {x} {y} {z}. */
public class AeroScoreboard extends Module {
    private final SettingGroup sg = settings.getDefaultGroup();
    private final Setting<String> title = sg.add(new StringSetting.Builder()
        .name("title").description("Scoreboard title.").defaultValue("AERO").build());
    private final Setting<String> lines = sg.add(new StringSetting.Builder()
        .name("lines").description("Lines separated by |  (vars: {name} {fps} {x} {y} {z})")
        .defaultValue("Player: {name}|FPS: {fps}|XYZ: {x} {y} {z}").build());
    private final Setting<Integer> offsetY = sg.add(new IntSetting.Builder()
        .name("offset-y").description("Vertical offset from the screen center.").defaultValue(0).sliderRange(-100, 100).build());

    public AeroScoreboard() { super(AeroCategory.HUD_V1, "aero-scoreboard", "Custom client-side sidebar overlay."); }

    private String fill(String s) {
        var p = mc.player;
        return s.replace("{name}", p.getName().getString())
            .replace("{fps}", String.valueOf(mc.getCurrentFps()))
            .replace("{x}", String.valueOf((int) p.getX()))
            .replace("{y}", String.valueOf((int) p.getY()))
            .replace("{z}", String.valueOf((int) p.getZ()));
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        if (mc.player == null) return;
        DrawContext ctx = event.drawContext;
        String[] raw = lines.get().split("\\|");
        String[] rows = new String[raw.length];
        String head = title.get();
        int w = AeroText.width(head);
        for (int i = 0; i < raw.length; i++) { rows[i] = fill(raw[i]); w = Math.max(w, AeroText.width(rows[i])); }
        w += 10;
        int rh = 10, h = rh * (rows.length + 1) + 5;
        int x2 = event.screenWidth - 4, x1 = x2 - w, y1 = (event.screenHeight - h) / 2 + offsetY.get(), y2 = y1 + h;
        ctx.fill(x1, y1, x2, y2, 0x90000000);
        AeroText.draw(ctx, head, x1 + (w - AeroText.width(head)) / 2, y1 + 3, 0xFF6FD3FF);
        int y = y1 + 4 + rh;
        for (String r : rows) { AeroText.draw(ctx, r, x1 + 5, y, 0xFFFFFFFF); y += rh; }
    }
}
