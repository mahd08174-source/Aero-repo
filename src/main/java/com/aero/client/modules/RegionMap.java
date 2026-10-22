package com.aero.client.modules;

import com.aero.client.AeroCategory;
import java.util.HashMap;
import java.util.Map;
import com.aero.client.core.events.Render2DEvent;
import com.aero.client.core.settings.DoubleSetting;
import com.aero.client.core.settings.IntSetting;
import com.aero.client.core.settings.Setting;
import com.aero.client.core.settings.SettingGroup;
import com.aero.client.core.Module;
import com.aero.client.core.events.EventHandler;
import net.minecraft.class_2487;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

public class RegionMap
extends Module {
    private static final int MAP_SIZE = 9;
    private static final int CELL_SIZE = 10;
    private static final int CELL_GAP = 1;
    private static final double REGION_SIZE = 50000.0;
    private static final double MAP_OFFSET = 225000.0;
    private static final String[] REGION_NAMES = new String[]{"EU Central", "EU West", "NA East", "NA West", "Asia", "Oceania"};
    private static final int[] RC = new int[]{-6304157, -16734621, -11555350, -13668678, -671166, -227325};
    private static final int[][] REGION_LAYOUT = new int[][]{{82, 5}, {100, 3}, {101, 3}, {102, 3}, {103, 2}, {104, 2}, {105, 2}, {106, 2}, {91, 2}, {83, 5}, {44, 3}, {75, 3}, {42, 3}, {41, 2}, {40, 2}, {39, 2}, {38, 2}, {92, 2}, {84, 5}, {45, 3}, {14, 3}, {13, 3}, {12, 2}, {11, 2}, {10, 2}, {37, 2}, {93, 2}, {85, 5}, {46, 5}, {74, 5}, {3, 3}, {2, 2}, {1, 2}, {25, 2}, {36, 2}, {94, 2}, {86, 4}, {47, 4}, {72, 4}, {71, 4}, {5, 2}, {4, 2}, {24, 2}, {35, 2}, {95, 2}, {87, 4}, {51, 1}, {17, 1}, {9, 0}, {8, 0}, {7, 0}, {23, 0}, {34, 0}, {96, 2}, {88, 4}, {54, 1}, {18, 1}, {61, 0}, {62, 0}, {21, 0}, {22, 0}, {33, 0}, {97, 0}, {89, 0}, {26, 1}, {27, 0}, {28, 0}, {29, 0}, {30, 0}, {59, 0}, {32, 0}, {98, 0}, {90, 0}, {107, 1}, {108, 1}, {109, 1}, {110, 1}, {111, 1}, {112, 1}, {113, 1}, {99, 0}};
    private static final int C_BG = -267580147;
    private static final int C_BORDER = 0x30FFFFFF;
    private static final int C_SHINE = 0x9FFFFFF;
    private static final int C_ACCENT = -14829228;
    private static final int C_TEXT = -1;
    private static final int C_SUB = -6645094;
    private static final int R = 8;
    private final SettingGroup sg;
    public final Setting<Integer> bgOpacity;
    public final Setting<Double> scale;
    private static final Map<Integer, RegionInfo> REGION_MAP = RegionMap.buildRegionMap();
    private int panelX;
    private int panelY;
    private boolean dragging;
    private int dragOffX;
    private int dragOffY;
    private boolean lastMouseDown;

    public RegionMap() {
        super(AeroCategory.HUD_V1, "region-map", "Draggable DonutSMP world region map, styled like SpotifyHUD.");
        this.sg = this.settings.getDefaultGroup();
        this.bgOpacity = this.sg.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("bg-opacity")).description("Background opacity 0\u2013255.")).defaultValue(230)).min(0).sliderMax(255).build());
        this.scale = this.sg.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("scale")).description("Map scale multiplier.")).defaultValue(1.0).min(0.5).max(3.0).sliderRange(0.5, 2.0).build());
        this.panelX = 12;
        this.panelY = 148;
    }

    public void onDeactivate() {
        this.dragging = false;
        this.lastMouseDown = false;
    }

    public class_2487 toTag() {
        class_2487 tag = super.toTag();
        if (tag == null) {
            tag = new class_2487();
        }
        tag.method_10569("panelX", this.panelX);
        tag.method_10569("panelY", this.panelY);
        return tag;
    }

    public Module fromTag(class_2487 tag) {
        super.fromTag(tag);
        this.panelX = tag.method_10550("panelX").orElse(this.panelX);
        this.panelY = tag.method_10550("panelY").orElse(this.panelY);
        return this;
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        this.render(event.drawContext);
    }

    private void render(class_332 ctx) {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
            return;
        }
        float sc = ((Double)this.scale.get()).floatValue();
        int gridW = 98;
        int legendW = 70;
        int pad = 8;
        int panelW = pad + gridW + pad + legendW + pad;
        int panelH = pad + 99 - 1 + pad + 14 + pad;
        int sw = this.mc.method_22683().method_4486();
        int sh = this.mc.method_22683().method_4502();
        this.panelX = class_3532.method_15340((int)this.panelX, (int)0, (int)Math.max(0, sw - (int)((float)panelW * sc)));
        this.panelY = class_3532.method_15340((int)this.panelY, (int)0, (int)Math.max(0, sh - (int)((float)panelH * sc)));
        this.handleDrag((int)((float)panelW * sc), (int)((float)panelH * sc));
        ctx.method_51448().pushMatrix();
        ctx.method_51448().translate((float)this.panelX, (float)this.panelY);
        ctx.method_51448().scale(sc, sc);
        int bgColor = (Integer)this.bgOpacity.get() << 24 | 0xD0D0D;
        this.drawRoundRect(ctx, 0, 0, panelW, panelH, 8, bgColor);
        this.drawRoundRect(ctx, 0, 0, panelW, 16, 8, 0x9FFFFFF);
        this.drawRoundRectBorder(ctx, 0, 0, panelW, panelH, 8, 0x30FFFFFF);
        this.drawRoundRectBorder(ctx, 0, 0, 40, 3, 1, 1344125268);
        int gridLeft = pad;
        int gridTop = pad;
        for (int row = 0; row < 9; ++row) {
            for (int col = 0; col < 9; ++col) {
                RegionInfo info = REGION_MAP.get(row * 9 + col);
                if (info == null) continue;
                int cx = gridLeft + col * 11;
                int cy = gridTop + row * 11;
                int typeIdx = class_3532.method_15340((int)info.regionType(), (int)0, (int)(RC.length - 1));
                int cellColor = RC[typeIdx] & 0xFFFFFF | 0xD8000000;
                ctx.method_25294(cx, cy, cx + 10, cy + 10, cellColor);
                this.drawTinyText(ctx, String.valueOf(info.regionId()), cx + 1, cy + 2);
            }
        }
        double px_ = this.mc.field_1724.method_23317();
        double pz_ = this.mc.field_1724.method_23321();
        double cellFX = class_3532.method_15350((double)((px_ + 225000.0) / 50000.0), (double)0.0, (double)8.99);
        double cellFZ = class_3532.method_15350((double)((pz_ + 225000.0) / 50000.0), (double)0.0, (double)8.99);
        int dotX = (int)Math.round((double)gridLeft + cellFX * 11.0);
        int dotY = (int)Math.round((double)gridTop + cellFZ * 11.0);
        ctx.method_25294(dotX - 2, dotY - 2, dotX + 3, dotY + 3, -1);
        ctx.method_25294(dotX - 1, dotY - 1, dotX + 2, dotY + 2, -14829228);
        int lx = gridLeft + gridW + pad;
        int ly = gridTop;
        ctx.method_51433(this.mc.field_1772, "Regions", lx, ly, -1, false);
        ly += 10;
        for (int t = 0; t < REGION_NAMES.length; ++t) {
            int typeColor = RC[t] & 0xFFFFFF | 0xFF000000;
            ctx.method_25294(lx, ly + 2, lx + 6, ly + 8, typeColor);
            this.drawSmallText(ctx, REGION_NAMES[t], lx + 9, ly + 1, -6645094);
            ly += 10;
        }
        int regionId = RegionMap.getRegionIdAt(px_, pz_);
        String regionName = RegionMap.getRegionTypeName(px_, pz_);
        this.drawSmallText(ctx, "R:" + String.valueOf(regionId >= 0 ? Integer.valueOf(regionId) : "?") + " " + regionName, lx, ly += 2, -1);
        this.drawSmallText(ctx, "X:" + (int)Math.floor(px_) + " Z:" + (int)Math.floor(pz_), lx, ly += 9, -6645094);
        ctx.method_51448().popMatrix();
    }

    private void drawRoundRect(class_332 ctx, int x, int y, int w, int h, int r, int color) {
        ctx.method_25294(x + r, y, x + w - r, y + h, color);
        ctx.method_25294(x, y + r, x + r, y + h - r, color);
        ctx.method_25294(x + w - r, y + r, x + w, y + h - r, color);
        ctx.method_25294(x, y, x + r, y + r, color);
        ctx.method_25294(x + w - r, y, x + w, y + r, color);
        ctx.method_25294(x, y + h - r, x + r, y + h, color);
        ctx.method_25294(x + w - r, y + h - r, x + w, y + h, color);
    }

    private void drawRoundRectBorder(class_332 ctx, int x, int y, int w, int h, int r, int color) {
        ctx.method_25294(x + r, y, x + w - r, y + 1, color);
        ctx.method_25294(x + r, y + h - 1, x + w - r, y + h, color);
        ctx.method_25294(x, y + r, x + 1, y + h - r, color);
        ctx.method_25294(x + w - 1, y + r, x + w, y + h - r, color);
    }

    private void drawSmallText(class_332 ctx, String text, int x, int y, int color) {
        Matrix3x2fStack pose = ctx.method_51448();
        pose.pushMatrix();
        pose.translate((float)x, (float)y);
        pose.scale(0.75f, 0.75f);
        ctx.method_51433(this.mc.field_1772, text, 0, 0, color, false);
        pose.popMatrix();
    }

    private void drawTinyText(class_332 ctx, String text, int x, int y) {
        Matrix3x2fStack pose = ctx.method_51448();
        pose.pushMatrix();
        pose.translate((float)x, (float)y);
        pose.scale(0.5f, 0.5f);
        ctx.method_51433(this.mc.field_1772, text, 0, 0, -1, false);
        pose.popMatrix();
    }

    private void handleDrag(int panelW, int panelH) {
        boolean overPanel;
        long window = this.mc.method_22683().method_4490();
        double mx = this.mc.field_1729.method_1603() * (double)this.mc.method_22683().method_4486() / (double)this.mc.method_22683().method_4480();
        double my = this.mc.field_1729.method_1604() * (double)this.mc.method_22683().method_4502() / (double)this.mc.method_22683().method_4507();
        boolean mouseDown = GLFW.glfwGetMouseButton((long)window, (int)0) == 1;
        boolean bl = overPanel = mx >= (double)this.panelX && mx <= (double)(this.panelX + panelW) && my >= (double)this.panelY && my <= (double)(this.panelY + panelH);
        if (mouseDown && !this.lastMouseDown && overPanel) {
            this.dragging = true;
            this.dragOffX = (int)mx - this.panelX;
            this.dragOffY = (int)my - this.panelY;
        }
        if (!mouseDown) {
            this.dragging = false;
        }
        if (this.dragging) {
            this.panelX = (int)mx - this.dragOffX;
            this.panelY = (int)my - this.dragOffY;
        }
        this.lastMouseDown = mouseDown;
    }

    private static int[] worldToGrid(double x, double z) {
        return new int[]{(int)Math.floor((x + 225000.0) / 50000.0), (int)Math.floor((z + 225000.0) / 50000.0)};
    }

    private static int getRegionIdAt(double x, double z) {
        int[] g = RegionMap.worldToGrid(x, z);
        if (g[0] < 0 || g[0] >= 9 || g[1] < 0 || g[1] >= 9) {
            return -1;
        }
        RegionInfo r = REGION_MAP.get(g[1] * 9 + g[0]);
        return r == null ? -1 : r.regionId();
    }

    private static String getRegionTypeName(double x, double z) {
        int[] g = RegionMap.worldToGrid(x, z);
        if (g[0] < 0 || g[0] >= 9 || g[1] < 0 || g[1] >= 9) {
            return "Unknown";
        }
        RegionInfo r = REGION_MAP.get(g[1] * 9 + g[0]);
        if (r == null) {
            return "Unknown";
        }
        int t = class_3532.method_15340((int)r.regionType(), (int)0, (int)(REGION_NAMES.length - 1));
        return REGION_NAMES[t];
    }

    private static Map<Integer, RegionInfo> buildRegionMap() {
        HashMap<Integer, RegionInfo> map = new HashMap<Integer, RegionInfo>();
        for (int i = 0; i < REGION_LAYOUT.length; ++i) {
            map.put(i, new RegionInfo(REGION_LAYOUT[i][0], REGION_LAYOUT[i][1]));
        }
        return map;
    }

    public record RegionInfo(int regionId, int regionType) {
    }
}

