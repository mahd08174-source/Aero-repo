package com.aero.client.modules;

import com.aero.client.AeroCategory;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import com.aero.client.core.events.Render3DEvent;
import com.aero.client.core.events.TickEvent;
import com.aero.client.core.render.ShapeMode;
import com.aero.client.core.settings.BoolSetting;
import com.aero.client.core.settings.ColorSetting;
import com.aero.client.core.settings.IntSetting;
import com.aero.client.core.settings.Setting;
import com.aero.client.core.settings.SettingGroup;
import com.aero.client.core.Module;
import com.aero.client.core.render.Color;
import com.aero.client.core.render.SettingColor;
import com.aero.client.core.events.EventHandler;
import net.minecraft.class_1923;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_2741;
import net.minecraft.class_2769;
import net.minecraft.class_2818;

public class SusChunkFinder
extends Module {
    public static final int CHUNKS_PER_TICK = 3;
    public static final int RESCAN_TICKS = 400;
    private final SettingGroup sg;
    public final Setting<Integer> sensitivity;
    public final Setting<Integer> simDistance;
    public final Setting<SettingColor> fillColor;
    public final Setting<SettingColor> lineColor;
    public final Setting<Boolean> detectJungleVines;
    public final Setting<Boolean> detectCaveVines;
    public final Setting<Boolean> detectKelp;
    public final Setting<Boolean> detectSweetBerries;
    public final Setting<Boolean> detectSugarCane;
    public final Setting<Boolean> detectCocoaBeans;
    public final Setting<Boolean> detectAmethyst;
    public final Map<class_1923, ChunkHit> hits;
    public final Set<class_1923> scanned;
    public final Set<class_1923> queued;
    public final ArrayDeque<class_1923> scanQueue;
    public int rescanTimer;

    public SusChunkFinder() {
        super(AeroCategory.CATEGORY, "sus-chunk-finder", "Finds suspicious chunks via natural block pattern analysis.");
        this.sg = this.settings.getDefaultGroup();
        this.sensitivity = this.sg.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("sensitivity")).description("Minimum stripped budding amethysts to flag.")).defaultValue(5)).min(1).sliderMax(20).build());
        this.simDistance = this.sg.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("sim-distance")).description("Scan radius in chunks.")).defaultValue(8)).min(1).sliderMax(20).build());
        this.fillColor = this.sg.add((Setting)((ColorSetting.Builder)new ColorSetting.Builder().name("fill-color")).defaultValue(new SettingColor(220, 30, 30, 53)).build());
        this.lineColor = this.sg.add((Setting)((ColorSetting.Builder)new ColorSetting.Builder().name("line-color")).defaultValue(new SettingColor(220, 30, 30, 160)).build());
        this.detectJungleVines = this.sg.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("jungle-vines")).defaultValue(true)).build());
        this.detectCaveVines = this.sg.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("cave-vines")).defaultValue(true)).build());
        this.detectKelp = this.sg.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("kelp")).defaultValue(true)).build());
        this.detectSweetBerries = this.sg.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("sweet-berries")).defaultValue(true)).build());
        this.detectSugarCane = this.sg.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("sugar-cane")).defaultValue(true)).build());
        this.detectCocoaBeans = this.sg.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("cocoa-beans")).defaultValue(true)).build());
        this.detectAmethyst = this.sg.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("amethyst")).defaultValue(true)).build());
        this.hits = new ConcurrentHashMap<class_1923, ChunkHit>();
        this.scanned = ConcurrentHashMap.newKeySet();
        this.queued = ConcurrentHashMap.newKeySet();
        this.scanQueue = new ArrayDeque();
        this.rescanTimer = 0;
    }

    public void onActivate() {
        this.clear();
    }

    public void onDeactivate() {
        this.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        class_1923 pos;
        if (this.mc.field_1687 == null || this.mc.field_1724 == null) {
            return;
        }
        if (++this.rescanTimer >= 400) {
            this.cleanUpFarHits();
            this.rescanTimer = 0;
        }
        this.enqueueChunks();
        for (int n = 0; n < 3 && (pos = this.scanQueue.poll()) != null; ++n) {
            this.queued.remove(pos);
            class_2818 chunk = this.mc.field_1687.method_8497(pos.field_9181, pos.field_9180);
            if (chunk == null || chunk.method_12223() || !this.scanned.add(pos)) continue;
            this.scanChunk(chunk, pos);
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (this.hits.isEmpty()) {
            return;
        }
        for (class_1923 pos : this.hits.keySet()) {
            double x1 = pos.method_8326();
            double z1 = pos.method_8328();
            event.renderer.box(x1, 63.0, z1, x1 + 16.0, 63.15, z1 + 16.0, (Color)this.fillColor.get(), (Color)this.lineColor.get(), ShapeMode.Both, 0);
        }
    }

    public void enqueueChunks() {
        class_1923 center = this.mc.field_1724.method_31476();
        int radius = Math.min((Integer)this.simDistance.get(), this.viewDist());
        for (int dx = -radius; dx <= radius; ++dx) {
            for (int dz = -radius; dz <= radius; ++dz) {
                class_1923 pos = new class_1923(center.field_9181 + dx, center.field_9180 + dz);
                if (this.scanned.contains(pos) || !this.queued.add(pos)) continue;
                this.scanQueue.offer(pos);
            }
        }
    }

    public void cleanUpFarHits() {
        if (this.mc.field_1724 == null) {
            return;
        }
        class_1923 center = this.mc.field_1724.method_31476();
        int maxDist = Math.max((Integer)this.simDistance.get(), this.viewDist()) + 4;
        this.hits.keySet().removeIf(p -> Math.abs(p.field_9181 - center.field_9181) > maxDist || Math.abs(p.field_9180 - center.field_9180) > maxDist);
        this.scanned.removeIf(p -> Math.abs(p.field_9181 - center.field_9181) > maxDist || Math.abs(p.field_9180 - center.field_9180) > maxDist);
        this.queued.removeIf(p -> Math.abs(p.field_9181 - center.field_9181) > maxDist || Math.abs(p.field_9180 - center.field_9180) > maxDist);
    }

    public void scanChunk(class_2818 chunk, class_1923 chunkPos) {
        class_2338.class_2339 cursor = new class_2338.class_2339();
        class_2338.class_2339 amCursor = new class_2338.class_2339();
        int originX = chunkPos.method_8326();
        int originZ = chunkPos.method_8328();
        int minY = this.mc.field_1687.method_31607();
        int maxY = minY + this.mc.field_1687.method_31605();
        int[] kelpRuns = new int[256];
        int[] caveVineRuns = new int[256];
        int[] vineRuns = new int[256];
        int[] sugarCaneRuns = new int[256];
        int ripeBerries = 0;
        int cocoaCount = 0;
        int strippedBudding = 0;
        for (int dx = 0; dx < 16; ++dx) {
            for (int dz = 0; dz < 16; ++dz) {
                int col = dx * 16 + dz;
                int kr = 0;
                int cvr = 0;
                int vr = 0;
                int scr = 0;
                int kb = 0;
                int cvb = 0;
                int vb = 0;
                int scb = 0;
                for (int y = minY; y <= maxY; ++y) {
                    class_2680 state;
                    cursor.method_10103(originX + dx, y, originZ + dz);
                    try {
                        state = chunk.method_8320((class_2338)cursor);
                    }
                    catch (Exception e) {
                        continue;
                    }
                    class_2248 block = state.method_26204();
                    if (block == class_2246.field_9993 || block == class_2246.field_10463) {
                        if (++kr > kb) {
                            kb = kr;
                        }
                    } else {
                        kr = 0;
                    }
                    if (block == class_2246.field_28675 || block == class_2246.field_28676) {
                        if (++cvr > cvb) {
                            cvb = cvr;
                        }
                    } else {
                        cvr = 0;
                    }
                    if (block == class_2246.field_10597) {
                        if (++vr > vb) {
                            vb = vr;
                        }
                    } else {
                        vr = 0;
                    }
                    if (block == class_2246.field_10424) {
                        if (++scr > scb) {
                            scb = scr;
                        }
                    } else {
                        scr = 0;
                    }
                    if (block == class_2246.field_16999 && state.method_28498((class_2769)class_2741.field_12497)) {
                        try {
                            if ((Integer)state.method_11654((class_2769)class_2741.field_12497) >= 3) {
                                ++ripeBerries;
                            }
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                    if (block == class_2246.field_10302) {
                        ++cocoaCount;
                    }
                    if (block != class_2246.field_27160 || this.hasAmethystGrowthOnAnyFace(amCursor, originX + dx, y, originZ + dz)) continue;
                    ++strippedBudding;
                }
                kelpRuns[col] = kb;
                caveVineRuns[col] = cvb;
                vineRuns[col] = vb;
                sugarCaneRuns[col] = scb;
            }
        }
        String reason = null;
        if (((Boolean)this.detectJungleVines.get()).booleanValue()) {
            for (int r : vineRuns) {
                if (r < 34 || r > 50) continue;
                reason = "Jungle Vines (" + r + " tall)";
                break;
            }
        }
        if (reason == null && ((Boolean)this.detectCaveVines.get()).booleanValue()) {
            for (int r : caveVineRuns) {
                if (r < 7 || r > 50) continue;
                reason = "Cave Vines (" + r + " tall)";
                break;
            }
        }
        if (reason == null && ((Boolean)this.detectKelp.get()).booleanValue()) {
            for (int r : kelpRuns) {
                if (r < 24 || r > 50) continue;
                reason = "Tall Kelp (" + r + " tall)";
                break;
            }
        }
        if (reason == null && ((Boolean)this.detectSweetBerries.get()).booleanValue() && ripeBerries >= 3 && ripeBerries <= 50) {
            reason = "Sweet Berries (" + ripeBerries + ")";
        }
        if (reason == null && ((Boolean)this.detectSugarCane.get()).booleanValue()) {
            for (int r : sugarCaneRuns) {
                if (r < 5 || r > 50) continue;
                reason = "Sugar Cane (" + r + " tall)";
                break;
            }
        }
        if (reason == null && ((Boolean)this.detectCocoaBeans.get()).booleanValue() && cocoaCount > 0) {
            reason = "Cocoa Beans (" + cocoaCount + ")";
        }
        if (reason == null && ((Boolean)this.detectAmethyst.get()).booleanValue() && strippedBudding >= Math.max(1, (Integer)this.sensitivity.get())) {
            reason = "Stripped Geode (" + strippedBudding + " budding)";
        }
        if (reason != null) {
            this.hits.putIfAbsent(chunkPos, new ChunkHit(chunkPos, reason));
        }
    }

    public boolean hasAmethystGrowthOnAnyFace(class_2338.class_2339 cursor, int x, int y, int z) {
        int[][] offsets;
        for (int[] off : offsets = new int[][]{{0, 1, 0}, {0, -1, 0}, {1, 0, 0}, {-1, 0, 0}, {0, 0, 1}, {0, 0, -1}}) {
            cursor.method_10103(x + off[0], y + off[1], z + off[2]);
            class_2248 b = this.mc.field_1687.method_8320((class_2338)cursor).method_26204();
            if (b != class_2246.field_27161 && b != class_2246.field_27162 && b != class_2246.field_27163 && b != class_2246.field_27164) continue;
            return true;
        }
        return false;
    }

    public int viewDist() {
        return this.mc.field_1690 == null ? 8 : (Integer)this.mc.field_1690.method_42503().method_41753();
    }

    public void clear() {
        this.hits.clear();
        this.scanned.clear();
        this.queued.clear();
        this.scanQueue.clear();
        this.rescanTimer = 0;
    }

    public record ChunkHit(class_1923 pos, String reason) {
    }
}

