package com.aero.client.gui;

import com.aero.client.AeroText;
import com.aero.client.api.Module;
import com.aero.client.api.Modules;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/** Aero ClickGUI: left click = toggle, right click = settings, drag header = move, Right Shift / Esc = close. */
public class AeroScreen extends Screen {
    static final int BG = 0xEE090E18, BORDER = 0xEBD2E6F5, DIVIDER = 0x46D2E6F5, HOVER = 0x14FFFFFF,
        TEXT = 0xFFF0F6FC, SUB = 0xFF8FA3B8, OFF = 0xFF5C6678, ACCENT = 0xFF6FD3FF, KNOB = 0xFFF2F8FE, DIM = 0x78000000, GLOW = 0x286FD3FF;
    private static final int W = 118, HEAD = 16, ROW = 14, GAP = 10;

    private static final String[][] LAYOUT = {
        {"COMBAT", "auto-totem", "auto-log"},
        {"MISC", "afk-fly", "auto-relog", "relog-helper", "custom-freecam", "render-distance-bypass", "staff-detector", "auto-sprint"},
        {"DONUT", "auto-rtp-home", "home-seter", "spawner-notifier", "spawner-chunk", "chunk-finder", "p3nis-finder",
            "sus-chunk-finder", "tuff-chunk-finder", "player-chunk-finder", "storage-density-scanner"},
        {"RENDER", "amethyst-esp", "spawner-tags", "staff-list", "spotify-hud", "region-map", "aero-scoreboard", "player-esp", "storage-esp", "fullbright"}
    };

    private static class Panel {
        String title; List<Module> mods = new ArrayList<>(); double x, y; boolean placed;
        int height() { return HEAD + 3 + mods.size() * ROW + 3; }
    }
    private static final List<Panel> panels = new ArrayList<>();
    private Panel dragging; private double dragDX, dragDY;

    public AeroScreen() {
        super(Text.literal("Aero Client"));
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
            for (Module m : Modules.get().all()) if (!placed.contains(m)) panels.get(1).mods.add(m);
        }
    }

    @Override protected void init() {
        double total = panels.size() * W + (panels.size() - 1) * GAP;
        double x = (width - total) / 2.0;
        double top = Math.max(60, height * 0.27);
        for (Panel p : panels) {
            if (!p.placed) { p.x = x; p.y = top; p.placed = true; }
            x += W + GAP;
        }
    }

    @Override public boolean shouldPause() { return false; }
    @Override public void renderBackground(DrawContext c, int mx, int my, float delta) { }

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        c.fill(0, 0, width, height, DIM);

        double topY = Double.MAX_VALUE;
        for (Panel p : panels) topY = Math.min(topY, p.y);
        String title = "- Aero Client -";
        int tx = (width - AeroText.width(title, "xuong_big")) / 2, ty = (int) topY - 38;
        for (int[] o : new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {1, 1}})
            AeroText.draw(c, title, "xuong_big", tx + o[0], ty + o[1], GLOW);
        AeroText.draw(c, title, "xuong_big", tx, ty, ACCENT);

        for (Panel p : panels) {
            int px = (int) p.x, py = (int) p.y, h = p.height();
            c.fill(px - 1, py - 1, px + W + 1, py + h + 1, BORDER);
            c.fill(px, py, px + W, py + h, BG);
            c.fill(px + 6, py + HEAD, px + W - 6, py + HEAD + 1, DIVIDER);
            AeroText.draw(c, p.title, px + 6, py + (HEAD - 9) / 2 + 1, TEXT);
            for (int i = 0; i < p.mods.size(); i++) {
                Module m = p.mods.get(i);
                int ry = py + HEAD + 3 + i * ROW;
                boolean hov = mx >= px && mx < px + W && my >= ry && my < ry + ROW;
                if (hov) { c.fill(px + 1, ry, px + W - 1, ry + ROW, HOVER); c.fill(px + 1, ry + 1, px + 3, ry + ROW - 1, ACCENT); }
                AeroText.draw(c, m.name.replace('-', ' ').toUpperCase(), px + 6, ry + (ROW - 9) / 2 + 1, TEXT);
                pill(c, px + W - 19, ry + (ROW - 6) / 2, m.isActive());
            }
        }
    }

    static void pill(DrawContext c, int x, int y, boolean on) {
        int col = on ? ACCENT : OFF;
        c.fill(x + 1, y, x + 11, y + 6, col);
        c.fill(x, y + 1, x + 12, y + 5, col);
        int kx = on ? x + 6 : x;
        c.fill(kx + 1, y, kx + 5, y + 6, KNOB);
        c.fill(kx, y + 1, kx + 6, y + 5, KNOB);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x(), my = click.y();
        int btn = click.button();
        for (int i = panels.size() - 1; i >= 0; i--) {
            Panel p = panels.get(i);
            if (mx < p.x || mx > p.x + W || my < p.y || my > p.y + p.height()) continue;
            if (my < p.y + HEAD) {
                if (btn == 0) { dragging = p; dragDX = mx - p.x; dragDY = my - p.y; panels.remove(i); panels.add(p); }
            } else {
                int idx = (int) ((my - p.y - HEAD - 3) / ROW);
                if (idx >= 0 && idx < p.mods.size()) {
                    Module m = p.mods.get(idx);
                    if (btn == 0) m.toggle();
                    else if (btn == 1 && !m.settings.all().isEmpty()) client.setScreen(new AeroSettingsScreen(this, m));
                }
            }
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double ox, double oy) {
        if (dragging != null) { dragging.x = click.x() - dragDX; dragging.y = click.y() - dragDY; return true; }
        return super.mouseDragged(click, ox, oy);
    }

    @Override public boolean mouseReleased(Click click) { dragging = null; return super.mouseReleased(click); }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.key() == GLFW.GLFW_KEY_RIGHT_SHIFT) { close(); return true; }
        return super.keyPressed(input);
    }
}
