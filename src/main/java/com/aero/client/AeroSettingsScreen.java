package com.aero.client;

import com.aero.client.core.Module;
import com.aero.client.core.ModuleConfig;
import com.aero.client.core.render.SettingColor;
import com.aero.client.core.settings.BoolSetting;
import com.aero.client.core.settings.ColorSetting;
import com.aero.client.core.settings.DoubleSetting;
import com.aero.client.core.settings.EnumSetting;
import com.aero.client.core.settings.IntSetting;
import com.aero.client.core.settings.Setting;
import com.aero.client.core.settings.SettingGroup;
import com.aero.client.core.settings.StringSetting;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_437;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Settings screen of one module (right click a module in the Aero ClickGUI).
 * Left click toggles / cycles, drag a slider to change numbers, ESC goes back to the ClickGUI.
 */
public class AeroSettingsScreen extends class_437 {
    private static final int BG = 0xEE090E18, BORDER = 0xEBD2E6F5, DIVIDER = 0x46D2E6F5, HOVER = 0x14FFFFFF,
        TEXT = 0xFFF0F6FC, DIMTEXT = 0xFF8C98AB, OFF = 0xFF5C6678, ACCENT = 0xFF6FD3FF, KNOB = 0xFFF2F8FE, DIM = 0x78000000,
        TRACK = 0xFF1B2433;
    private static final int W = 260, HEAD = 18, ROW = 16, PAD = 6;

    private final Module module;
    private final List<Row> rows = new ArrayList<>();
    private double scroll;
    private Row dragging;
    private boolean prevL = true, prevR = true;

    private abstract static class Row {
        int height = ROW;
        boolean header;
        class_342 field;

        abstract void render(class_332 ctx, int x, int y, int w, boolean hovered, double mx);

        /** Returns true when the click was consumed. */
        boolean click(double mx, int x, int w, boolean left) { return false; }

        void drag(double mx, int x, int w) {}
    }

    public AeroSettingsScreen(Module module) {
        super(class_2561.method_43470(module.title));
        this.module = module;
    }

    @Override
    protected void method_25426() {
        rows.clear();
        method_37067();
        for (SettingGroup g : module.settings.getGroups()) {
            if (g.getAll().isEmpty()) continue;
            rows.add(headerRow(g.name));
            for (Setting<?> s : g.getAll()) addSettingRows(s);
        }
        if (rows.isEmpty()) rows.add(headerRow("No settings"));
    }

    private Row headerRow(String title) {
        Row r = new Row() {
            @Override
            void render(class_332 ctx, int x, int y, int w, boolean hovered, double mx) {
                AeroText.draw(ctx, title.toUpperCase(), x + PAD, y + 4, ACCENT);
                ctx.method_25294(x + PAD, y + ROW - 1, x + w - PAD, y + ROW, DIVIDER);
            }
        };
        r.header = true;
        return r;
    }

    private void addSettingRows(Setting<?> s) {
        if (s instanceof BoolSetting b) {
            rows.add(new Row() {
                @Override
                void render(class_332 ctx, int x, int y, int w, boolean hovered, double mx) {
                    AeroText.draw(ctx, label(b.name), x + PAD, y + 4, TEXT);
                    toggle(ctx, x + w - 19 - PAD, y + (ROW - 6) / 2, b.get());
                }

                @Override
                boolean click(double mx, int x, int w, boolean left) {
                    b.set(!b.get());
                    ModuleConfig.markDirty();
                    return true;
                }
            });
        } else if (s instanceof IntSetting i) {
            rows.add(slider(i.name, () -> (double) i.get(), v -> i.set((int) Math.round(v)), i.sliderMin, i.sliderMax, 0));
        } else if (s instanceof DoubleSetting d) {
            rows.add(slider(d.name, d::get, d::set, d.sliderMin, d.sliderMax, 2));
        } else if (s instanceof EnumSetting<?> e) {
            rows.add(new Row() {
                @Override
                void render(class_332 ctx, int x, int y, int w, boolean hovered, double mx) {
                    AeroText.draw(ctx, label(e.name), x + PAD, y + 4, TEXT);
                    String v = e.get().name();
                    AeroText.draw(ctx, v, x + w - PAD - AeroText.width(v), y + 4, ACCENT);
                }

                @Override
                boolean click(double mx, int x, int w, boolean left) {
                    e.cycle(left);
                    ModuleConfig.markDirty();
                    return true;
                }
            });
        } else if (s instanceof ColorSetting c) {
            rows.add(new Row() {
                @Override
                void render(class_332 ctx, int x, int y, int w, boolean hovered, double mx) {
                    AeroText.draw(ctx, label(c.name), x + PAD, y + 4, TEXT);
                    SettingColor col = c.get();
                    int sx = x + w - PAD - 22;
                    ctx.method_25294(sx - 1, y + 2, sx + 23, y + ROW - 2, BORDER);
                    ctx.method_25294(sx, y + 3, sx + 22, y + ROW - 3, (0xFF << 24) | (col.r << 16) | (col.g << 8) | col.b);
                }
            });
            String[] names = {"R", "G", "B", "A"};
            for (int ch = 0; ch < 4; ch++) {
                final int idx = ch;
                rows.add(slider("  " + names[ch], () -> (double) component(c.get(), idx), v -> {
                    SettingColor cur = c.get();
                    int val = (int) Math.round(v);
                    SettingColor next = new SettingColor(cur.r, cur.g, cur.b, cur.a);
                    if (idx == 0) next.r = val;
                    else if (idx == 1) next.g = val;
                    else if (idx == 2) next.b = val;
                    else next.a = val;
                    c.set(next);
                }, 0, 255, 0));
            }
        } else if (s instanceof StringSetting str) {
            Row r = new Row() {
                @Override
                void render(class_332 ctx, int x, int y, int w, boolean hovered, double mx) {
                    AeroText.draw(ctx, label(str.name), x + PAD, y + 3, TEXT);
                }
            };
            r.height = ROW + 16;
            class_342 f = new class_342(class_310.method_1551().field_1772, 0, 0, W - PAD * 2, 14, class_2561.method_43470(str.name));
            f.method_1880(1024);
            f.method_1852(str.get());
            f.method_1863(text -> {
                str.set(text);
                ModuleConfig.markDirty();
            });
            r.field = f;
            method_37063(f);
            rows.add(r);
        }
    }

    private static int component(SettingColor c, int idx) {
        return idx == 0 ? c.r : idx == 1 ? c.g : idx == 2 ? c.b : c.a;
    }

    private static String label(String name) { return name.replace('-', ' ').toUpperCase(); }

    private static void toggle(class_332 ctx, int tgx, int tgy, boolean on) {
        int c = on ? ACCENT : OFF;
        ctx.method_25294(tgx + 1, tgy, tgx + 11, tgy + 6, c);
        ctx.method_25294(tgx, tgy + 1, tgx + 12, tgy + 5, c);
        int kx = on ? tgx + 6 : tgx;
        ctx.method_25294(kx + 1, tgy, kx + 5, tgy + 6, KNOB);
        ctx.method_25294(kx, tgy + 1, kx + 6, tgy + 5, KNOB);
    }

    private Row slider(String name, Supplier<Double> get, Consumer<Double> set, double min, double max, int decimals) {
        return new Row() {
            private int barX(int x) { return x + W / 2 - 10; }

            private int barW(int w) { return w / 2 - 10 - PAD; }

            @Override
            void render(class_332 ctx, int x, int y, int w, boolean hovered, double mx) {
                AeroText.draw(ctx, label(name), x + PAD, y + 4, TEXT);
                int bx = barX(x), bw = barW(w), by = y + 5;
                double v = get.get();
                double t = Math.max(0, Math.min(1, (v - min) / (max - min)));
                ctx.method_25294(bx, by, bx + bw, by + 6, TRACK);
                ctx.method_25294(bx, by, bx + (int) (bw * t), by + 6, ACCENT);
                String txt = decimals == 0 ? Long.toString(Math.round(v)) : String.format("%." + decimals + "f", v);
                AeroText.draw(ctx, txt, bx + bw - AeroText.width(txt) - 2, y + 4, KNOB);
            }

            @Override
            boolean click(double mx, int x, int w, boolean left) {
                if (!left) return false;
                dragging = this;
                drag(mx, x, w);
                return true;
            }

            @Override
            void drag(double mx, int x, int w) {
                double t = Math.max(0, Math.min(1, (mx - barX(x)) / barW(w)));
                double v = min + (max - min) * t;
                if (decimals > 0) v = Math.round(v * 100.0) / 100.0;
                set.accept(v);
                ModuleConfig.markDirty();
            }
        };
    }

    private int contentHeight() {
        int h = 0;
        for (Row r : rows) h += r.height;
        return h;
    }

    @Override
    public boolean method_25421() { return false; }

    @Override
    public void method_25419() {
        ModuleConfig.markDirty();
        class_310 mc = this.field_22787 != null ? this.field_22787 : class_310.method_1551();
        mc.method_1507(new AeroGui());
    }

    // mouse wheel (no @Override so a signature change can never break the build)
    public boolean method_25401(double mouseX, double mouseY, double horizontal, double vertical) {
        scroll -= vertical * 16.0;
        return true;
    }

    @Override
    public void method_25394(class_332 ctx, int mxI, int myI, float delta) {
        class_310 mc = class_310.method_1551();
        long win = mc.method_22683().method_4490();
        double mx = mc.field_1729.method_1603() * (double) mc.method_22683().method_4486() / (double) mc.method_22683().method_4480();
        double my = mc.field_1729.method_1604() * (double) mc.method_22683().method_4502() / (double) mc.method_22683().method_4507();

        boolean l = GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean r = GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        if (GLFW.glfwGetKey(win, GLFW.GLFW_KEY_DOWN) == GLFW.GLFW_PRESS) scroll += 3;
        if (GLFW.glfwGetKey(win, GLFW.GLFW_KEY_UP) == GLFW.GLFW_PRESS) scroll -= 3;

        int panelH = Math.min(this.field_22790 - 40, HEAD + 6 + contentHeight() + 6);
        int px = (this.field_22789 - W) / 2, py = (this.field_22790 - panelH) / 2;
        int listTop = py + HEAD + 3, listBottom = py + panelH - 3;
        double maxScroll = Math.max(0, contentHeight() - (listBottom - listTop));
        scroll = Math.max(0, Math.min(maxScroll, scroll));

        if (!l) dragging = null;
        if (dragging != null) dragging.drag(mx, px, W);

        boolean pressL = l && !prevL, pressR = r && !prevR;
        prevL = l;
        prevR = r;
        if ((pressL || pressR) && mx >= px && mx <= px + W && my >= listTop && my < listBottom) {
            int y = listTop - (int) scroll;
            for (Row row : rows) {
                if (my >= y && my < y + row.height && row.field == null && !row.header) {
                    if (row.click(mx, px, W, pressL)) break;
                }
                y += row.height;
            }
        }

        ctx.method_25294(0, 0, this.field_22789, this.field_22790, DIM);
        ctx.method_25294(px - 1, py - 1, px + W + 1, py + panelH + 1, BORDER);
        ctx.method_25294(px, py, px + W, py + panelH, BG);
        AeroText.draw(ctx, module.title.toUpperCase(), px + PAD, py + (HEAD - 9) / 2 + 1, TEXT);
        String hint = "ESC = back";
        AeroText.draw(ctx, hint, px + W - PAD - AeroText.width(hint), py + (HEAD - 9) / 2 + 1, DIMTEXT);
        ctx.method_25294(px + PAD, py + HEAD, px + W - PAD, py + HEAD + 1, DIVIDER);

        ctx.method_44379(px, listTop, px + W, listBottom);
        int y = listTop - (int) scroll;
        for (Row row : rows) {
            boolean visible = y + row.height > listTop && y < listBottom;
            boolean hov = mx >= px && mx <= px + W && my >= y && my < y + row.height && !row.header;
            if (visible) {
                if (hov) ctx.method_25294(px + 1, y, px + W - 1, y + row.height, HOVER);
                row.render(ctx, px, y, W, hov, mx);
            }
            if (row.field != null) {
                row.field.method_46421(px + PAD);
                row.field.method_46419(y + ROW + 1);
                row.field.field_22764 = visible && y >= listTop - 2 && y + row.height <= listBottom + 2;
            }
            y += row.height;
        }
        super.method_25394(ctx, mxI, myI, delta);
        ctx.method_44380();
    }
}
