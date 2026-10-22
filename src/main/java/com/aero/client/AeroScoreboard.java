package com.aero.client;

import com.aero.client.core.events.Render2DEvent;
import com.aero.client.core.settings.IntSetting;
import com.aero.client.core.settings.Setting;
import com.aero.client.core.settings.SettingGroup;
import com.aero.client.core.settings.StringSetting;
import com.aero.client.core.Module;
import com.aero.client.core.events.EventHandler;
import net.minecraft.class_332;

/**
 * Local custom sidebar overlay (cosmetic HUD). Lines are separated by '|' and may use
 * {x} {y} {z}. Defaults show live values from the game.
 */
public class AeroScoreboard extends Module {
    private final SettingGroup sg = this.settings.getDefaultGroup();
    private final Setting<String> title = sg.add(new StringSetting.Builder()
        .name("title").description("Scoreboard title.").defaultValue("AERO").build());
    private final Setting<String> lines = sg.add(new StringSetting.Builder()
        .name("lines").description("Lines separated by |  (vars: {x} {y} {z})")
        .defaultValue("Coordinates|X: {x}|Y: {y}|Z: {z}").build());
    private final Setting<Integer> offsetY = sg.add(new IntSetting.Builder()
        .name("offset-y").description("Vertical offset from center.").defaultValue(0).sliderRange(-100, 100).build());

    public AeroScoreboard() {
        super(AeroCategory.HUD_V1, "aero-scoreboard", "Custom client-side sidebar overlay.");
    }

    private String fill(String s) {
        var p = mc.field_1724;
        return s.replace("{x}", String.valueOf((int) p.method_23317()))
            .replace("{y}", String.valueOf((int) p.method_23318()))
            .replace("{z}", String.valueOf((int) p.method_23321()));
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        if (mc.field_1724 == null) return;
        class_332 ctx = event.drawContext;
        String[] raw = lines.get().split("\\|");
        String[] rows = new String[raw.length];
        String head = title.get();
        int w = AeroText.width(head);
        for (int i = 0; i < raw.length; i++) { rows[i] = fill(raw[i]); w = Math.max(w, AeroText.width(rows[i])); }
        w += 10;
        int rh = 10, h = rh * (rows.length + 1) + 5;
        int sw = mc.method_22683().method_4486(), sh = mc.method_22683().method_4502();
        int x2 = sw - 4, x1 = x2 - w, y1 = (sh - h) / 2 + offsetY.get(), y2 = y1 + h;
        ctx.method_25294(x1, y1, x2, y2, 0x90000000);
        AeroText.draw(ctx, head, x1 + (w - AeroText.width(head)) / 2, y1 + 3, 0xFF6FD3FF);
        int y = y1 + 4 + rh;
        for (String r : rows) { AeroText.draw(ctx, r, x1 + 5, y, 0xFFFFFFFF); y += rh; }
    }
}
