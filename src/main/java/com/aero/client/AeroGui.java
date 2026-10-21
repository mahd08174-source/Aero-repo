package com.aero.client;

import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Aero ClickGUI: COMBAT / MISC / DONUT / RENDER panels.
 * Left click = toggle, right click = module settings (Meteor's settings screen), drag header = move,
 * RIGHT SHIFT or ESC = close. Input is polled via GLFW (same approach as the ported HUD modules).
 */
public class AeroGui extends class_437 {
    // light-blue palette
    private static final int BG = 0xEE090E18, BORDER = 0xEBD2E6F5, DIVIDER = 0x46D2E6F5, HOVER = 0x14FFFFFF,
        TEXT = 0xFFF0F6FC, OFF = 0xFF5C6678, ACCENT = 0xFF6FD3FF, KNOB = 0xFFF2F8FE, DIM = 0x78000000;
    private static final int GLOW = 0x286FD3FF;
    private static final int W = 118, HEAD = 16, ROW = 14, GAP = 10;

    private static final String[][] LAYOUT = {
        {"COMBAT", "anchor-aura", "crystal-aura", "auto-trap", "auto-totem", "offhand", "surround", "criticals", "kill-aura", "auto-armor"},
        {"MISC", "afk-fly", "auto-relog", "relog-helper", "custom-freecam", "render-distance-bypass", "staff-detector", "auto-eat", "auto-log", "auto-tool"},
        {"DONUT", "auto-rtp-home", "home-seter", "spawner-notifier", "spawner-chunk", "chunk-finder", "p3nis-finder", "sus-chunk-finder",
            "tuff-chunk-finder", "player-chunk-finder", "storage-density-scanner"},
        {"RENDER", "amethyst-esp", "spawner-tags", "staff-list", "spotify-hud", "region-map", "aero-scoreboard", "esp", "storage-esp", "fullbright"}
    };

    private static class Panel {
        String title; List<Module> mods = new ArrayList<>(); double x, y; boolean placed;
        int height() { return HEAD + 3 + mods.size() * ROW + 3; }
    }
    private static final List<Panel> panels = new ArrayList<>();
    private Panel dragging; private double dragDX, dragDY;
    private boolean prevL = true, prevR = true, prevKey = true;

    public AeroGui() {
        super(class_2561.method_43470("Aero Client"));
        if (panels.isEmpty()) {
            List<Module> placed = new ArrayList<>();
            for (String[] def : LAYOUT) {
                Panel p = new Panel(); p.title = def[0];
                for (int i = 1; i < def.length; i++) {
                    Module m = Modules.get().get(def[i]);
                    if (m != null) { p.mods.add(m); placed.add(m); }
                }
                panels.add(p);
            }
            // any Aero module not listed above goes into MISC so nothing is unreachable
            for (meteordevelopment.meteorclient.systems.modules.Category c :
                new meteordevelopment.meteorclient.systems.modules.Category[]{AeroCategory.CATEGORY, AeroCategory.MISC_V2, AeroCategory.HUD_V1})
                for (Module m : Modules.get().getGroup(c)) if (!placed.contains(m)) panels.get(1).mods.add(m);
        }
    }

    protected void method_25426() { // init
        double total = panels.size() * W + (panels.size() - 1) * GAP;
        double x = (this.field_22789 - total) / 2.0;
        for (Panel p : panels) {
            if (!p.placed) { p.x = x; p.y = Math.max(50, this.field_22790 / 2.0 - 80); p.placed = true; }
            x += W + GAP;
        }
    }

    public boolean method_25421() { return false; } // don't pause singleplayer

    public void method_25394(class_332 ctx, int mxI, int myI, float delta) { // render
        class_310 mc = class_310.method_1551();
        long win = mc.method_22683().method_4490();
        double mx = mc.field_1729.method_1603() * (double) mc.method_22683().method_4486() / (double) mc.method_22683().method_4480();
        double my = mc.field_1729.method_1604() * (double) mc.method_22683().method_4502() / (double) mc.method_22683().method_4507();

        boolean l = GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean r = GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        boolean key = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
        if (key && !prevKey) { mc.method_1507(null); return; }
        prevKey = key;

        if (!l) dragging = null;
        if (dragging != null) { dragging.x = mx - dragDX; dragging.y = my - dragDY; }
        if ((l && !prevL) || (r && !prevR)) {
            boolean left = l && !prevL;
            for (int i = panels.size() - 1; i >= 0; i--) {
                Panel p = panels.get(i);
                if (mx < p.x || mx > p.x + W || my < p.y || my > p.y + p.height()) continue;
                if (my < p.y + HEAD) {
                    if (left) { dragging = p; dragDX = mx - p.x; dragDY = my - p.y; panels.remove(i); panels.add(p); }
                } else {
                    int idx = (int) ((my - p.y - HEAD - 3) / ROW);
                    if (idx >= 0 && idx < p.mods.size()) {
                        Module m = p.mods.get(idx);
                        if (left) m.toggle(); else mc.method_1507(GuiThemes.get().moduleScreen(m));
                    }
                }
                break;
            }
        }
        prevL = l; prevR = r;

        ctx.method_25294(0, 0, this.field_22789, this.field_22790, DIM);

        // title: big Xuong, light blue with a soft glow
        String title = "- Aero Client -";
        int tx = (this.field_22789 - AeroText.width(title, "xuong_big")) / 2, ty = 10;
        for (int[] o : new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {1, 1}})
            AeroText.draw(ctx, title, "xuong_big", tx + o[0], ty + o[1], GLOW);
        AeroText.draw(ctx, title, "xuong_big", tx, ty, ACCENT);

        for (Panel p : panels) {
            int px = (int) p.x, py = (int) p.y, h = p.height();
            ctx.method_25294(px - 1, py - 1, px + W + 1, py + h + 1, BORDER);
            ctx.method_25294(px, py, px + W, py + h, BG);
            ctx.method_25294(px + 6, py + HEAD, px + W - 6, py + HEAD + 1, DIVIDER);
            AeroText.draw(ctx, p.title, px + 6, py + (HEAD - 9) / 2 + 1, TEXT);

            for (int i = 0; i < p.mods.size(); i++) {
                Module m = p.mods.get(i);
                int ry = py + HEAD + 3 + i * ROW;
                boolean hov = mx >= px && mx <= px + W && my >= ry && my < ry + ROW;
                if (hov) { ctx.method_25294(px + 1, ry, px + W - 1, ry + ROW, HOVER); ctx.method_25294(px + 1, ry + 1, px + 3, ry + ROW - 1, ACCENT); }
                AeroText.draw(ctx, m.name.replace('-', ' ').toUpperCase(), px + 6, ry + (ROW - 9) / 2 + 1, TEXT);

                boolean on = m.isActive();
                int tgx = px + W - 19, tgy = ry + (ROW - 6) / 2, c = on ? ACCENT : OFF;
                ctx.method_25294(tgx + 1, tgy, tgx + 11, tgy + 6, c);
                ctx.method_25294(tgx, tgy + 1, tgx + 12, tgy + 5, c);
                int kx = on ? tgx + 6 : tgx;
                ctx.method_25294(kx + 1, tgy, kx + 5, tgy + 6, KNOB);
                ctx.method_25294(kx, tgy + 1, kx + 6, tgy + 5, KNOB);
            }
        }
    }
}
