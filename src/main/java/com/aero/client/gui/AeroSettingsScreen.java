package com.aero.client.gui;

import com.aero.client.AeroText;
import com.aero.client.api.*;
import com.aero.client.api.Module;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/** Per-module settings: toggles, sliders, enum / color cycling. Strings & hex colors: /aero set <module> <setting> <value>. */
public class AeroSettingsScreen extends Screen {
    private static final int[] PALETTE = {0x6FD3FF, 0x39FF7A, 0xFF5C5C, 0xFFA61A, 0xB57BFF, 0xFFFFFF, 0xFFE14D, 0xFF7BD5};
    private static final int PW = 250, HEAD = 22, ROW = 17;

    private final Screen parent;
    private final Module module;
    private final List<Setting<?>> list = new ArrayList<>();
    private double scroll;
    private Setting<?> dragging;
    private int px, py, ph;

    public AeroSettingsScreen(Screen parent, Module module) {
        super(Text.literal(module.title));
        this.parent = parent; this.module = module; list.addAll(module.settings.all());
    }

    @Override protected void init() {
        ph = Math.min(height - 40, HEAD + 6 + list.size() * ROW + 22);
        px = (width - PW) / 2; py = (height - ph) / 2;
    }

    @Override public boolean shouldPause() { return false; }
    @Override public void renderBackground(DrawContext c, int mx, int my, float delta) { }
    @Override public void close() { client.setScreen(parent); }

    private int viewH() { return ph - HEAD - 22; }
    private double maxScroll() { return Math.max(0, list.size() * ROW - viewH()); }

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        c.fill(0, 0, width, height, AeroScreen.DIM);
        c.fill(px - 1, py - 1, px + PW + 1, py + ph + 1, AeroScreen.BORDER);
        c.fill(px, py, px + PW, py + ph, AeroScreen.BG);
        AeroText.draw(c, module.title.toUpperCase(), px + 8, py + 7, AeroScreen.ACCENT);
        AeroScreen.pill(c, px + PW - 20, py + 8, module.isActive());
        c.fill(px + 6, py + HEAD - 2, px + PW - 6, py + HEAD - 1, AeroScreen.DIVIDER);

        int top = py + HEAD, bottom = py + ph - 22;
        Setting<?> hover = null;
        for (int i = 0; i < list.size(); i++) {
            int ry = top + i * ROW - (int) scroll;
            if (ry < top || ry + ROW > bottom) continue;
            Setting<?> s = list.get(i);
            boolean hov = mx >= px && mx < px + PW && my >= ry && my < ry + ROW;
            if (hov) { hover = s; c.fill(px + 1, ry, px + PW - 1, ry + ROW, AeroScreen.HOVER); }
            AeroText.draw(c, s.name.replace('-', ' ').toUpperCase(), px + 8, ry + 4, AeroScreen.TEXT);
            drawWidget(c, s, ry);
        }
        if (maxScroll() > 0) {
            int th = Math.max(12, (int) (viewH() * (viewH() / (double) (list.size() * ROW))));
            int ty = top + (int) ((viewH() - th) * (scroll / maxScroll()));
            c.fill(px + PW - 3, ty, px + PW - 1, ty + th, AeroScreen.ACCENT);
        }
        c.fill(px + 6, bottom, px + PW - 6, bottom + 1, AeroScreen.DIVIDER);
        String hint = hover != null && !hover.description.isEmpty() ? hover.description : "Strings / colors: /aero set " + module.name + " <setting> <value>";
        while (hint.length() > 3 && AeroText.width(hint) > PW - 16) hint = hint.substring(0, hint.length() - 4) + "...";
        AeroText.draw(c, hint, px + 8, bottom + 7, AeroScreen.SUB);
    }

    private int wx() { return px + PW - 98; }

    private void drawWidget(DrawContext c, Setting<?> s, int ry) {
        int x = wx();
        if (s instanceof BoolSetting b) AeroScreen.pill(c, px + PW - 20, ry + 5, b.get());
        else if (s instanceof IntSetting i) slider(c, x, ry, (i.get() - i.sliderMin) / (double) Math.max(1, i.sliderMax - i.sliderMin), s.display());
        else if (s instanceof DoubleSetting d) slider(c, x, ry, (d.get() - d.sliderMin) / Math.max(1e-9, d.sliderMax - d.sliderMin), s.display());
        else if (s instanceof ColorSetting col) {
            c.fill(x + 56, ry + 3, x + 90, ry + ROW - 3, 0xFF000000 | (col.get().getPacked() & 0xFFFFFF));
            AeroText.draw(c, col.display(), x, ry + 4, AeroScreen.SUB);
        } else {
            String v = s.display();
            while (v.length() > 3 && AeroText.width(v) > 92) v = v.substring(0, v.length() - 4) + "...";
            AeroText.draw(c, v, x + 92 - AeroText.width(v), ry + 4, AeroScreen.ACCENT);
        }
    }

    private void slider(DrawContext c, int x, int ry, double frac, String text) {
        frac = Math.max(0, Math.min(1, frac));
        c.fill(x, ry + 11, x + 90, ry + 13, AeroScreen.OFF);
        c.fill(x, ry + 11, x + (int) (90 * frac), ry + 13, AeroScreen.ACCENT);
        c.fill(x + (int) (90 * frac) - 1, ry + 9, x + (int) (90 * frac) + 1, ry + 15, AeroScreen.KNOB);
        AeroText.draw(c, text, x + 90 - AeroText.width(text), ry + 1, AeroScreen.TEXT);
    }

    private void applySlider(Setting<?> s, double mx) {
        double f = Math.max(0, Math.min(1, (mx - wx()) / 90.0));
        if (s instanceof IntSetting i) i.set((int) Math.round(i.sliderMin + f * (i.sliderMax - i.sliderMin)));
        else if (s instanceof DoubleSetting d) d.set(Math.round((d.sliderMin + f * (d.sliderMax - d.sliderMin)) * 100.0) / 100.0);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x(), my = click.y();
        if (mx >= px + PW - 22 && mx < px + PW - 4 && my >= py + 6 && my < py + 16) { module.toggle(); return true; }
        int top = py + HEAD, bottom = py + ph - 22;
        for (int i = 0; i < list.size(); i++) {
            int ry = top + i * ROW - (int) scroll;
            if (ry < top || ry + ROW > bottom) continue;
            if (mx < px || mx >= px + PW || my < ry || my >= ry + ROW) continue;
            Setting<?> s = list.get(i);
            if (s instanceof BoolSetting b) b.set(!b.get());
            else if (s instanceof IntSetting || s instanceof DoubleSetting) { dragging = s; applySlider(s, mx); }
            else if (s instanceof EnumSetting<?> e) e.cycle();
            else if (s instanceof ColorSetting col) {
                int cur = col.get().getPacked() & 0xFFFFFF, idx = 0;
                for (int k = 0; k < PALETTE.length; k++) if (PALETTE[k] == cur) idx = k + 1;
                int n = PALETTE[idx % PALETTE.length];
                col.set(new SettingColor((n >> 16) & 255, (n >> 8) & 255, n & 255, col.get().a));
            }
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override public boolean mouseDragged(Click click, double ox, double oy) {
        if (dragging != null) { applySlider(dragging, click.x()); return true; }
        return super.mouseDragged(click, ox, oy);
    }
    @Override public boolean mouseReleased(Click click) { dragging = null; return super.mouseReleased(click); }

    @Override public boolean mouseScrolled(double mx, double my, double h, double v) {
        scroll = Math.max(0, Math.min(maxScroll(), scroll - v * ROW));
        return true;
    }

    @Override public boolean keyPressed(KeyInput input) {
        if (input.key() == GLFW.GLFW_KEY_RIGHT_SHIFT) { client.setScreen(null); return true; }
        return super.keyPressed(input);
    }
}
