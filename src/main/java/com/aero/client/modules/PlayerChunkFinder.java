package com.aero.client.modules;

import com.aero.client.AeroCategory;
import java.util.LinkedHashMap;
import java.util.Map;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.ChunkDataEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1923;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2680;
import net.minecraft.class_2741;
import net.minecraft.class_2769;
import net.minecraft.class_2818;

public class PlayerChunkFinder
extends Module {
    private final SettingGroup sg;
    public final Setting<Boolean> detectUnnatural;
    public final Setting<Integer> renderRadius;
    public final Setting<Double> renderY;
    public final Setting<Boolean> filledEsp;
    public final Setting<SettingColor> fillColor;
    public final Setting<SettingColor> lineColor;
    private final Map<class_1923, String> modifiedChunks;

    public PlayerChunkFinder() {
        super(AeroCategory.CATEGORY, "player-chunk-finder", "Detects underground player-modified chunks via incoming chunk packets.");
        this.sg = this.settings.getDefaultGroup();
        this.detectUnnatural = this.sg.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("detect-unnatural")).description("Detect cobblestone, planks, torches, rails, containers underground.")).defaultValue(true)).build());
        this.renderRadius = this.sg.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("render-radius")).description("Render grid radius in chunks.")).defaultValue(8)).min(1).sliderMax(32).build());
        this.renderY = this.sg.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("render-y")).description("Y level to draw the slab at.")).defaultValue(-60.0).min(-64.0).max(320.0).sliderRange(-64.0, 320.0).build());
        this.filledEsp = this.sg.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("filled-esp")).defaultValue(true)).build());
        this.fillColor = this.sg.add((Setting)((ColorSetting.Builder)new ColorSetting.Builder().name("fill-color")).defaultValue(new SettingColor(255, 82, 82, 80)).build());
        this.lineColor = this.sg.add((Setting)((ColorSetting.Builder)new ColorSetting.Builder().name("line-color")).defaultValue(new SettingColor(255, 82, 82, 255)).build());
        this.modifiedChunks = new LinkedHashMap<class_1923, String>();
    }

    public void onActivate() {
        this.modifiedChunks.clear();
    }

    public void onDeactivate() {
        this.modifiedChunks.clear();
    }

    @EventHandler
    private void onChunkData(ChunkDataEvent event) {
        if (this.mc.field_1687 == null || this.mc.field_1724 == null) {
            return;
        }
        class_2818 chunk = event.chunk();
        class_1923 chunkPos = chunk.method_12004();
        if (this.modifiedChunks.containsKey(chunkPos)) {
            return;
        }
        String reason = this.scanChunk(chunkPos, chunk);
        if (reason == null) {
            return;
        }
        this.modifiedChunks.put(chunkPos, reason);
        this.info("Modified chunk at " + chunkPos.field_9181 + ", " + chunkPos.field_9180 + " \u00a77(" + reason + ")", new Object[0]);
    }

    private String scanChunk(class_1923 chunkPos, class_2818 chunk) {
        int originX = chunkPos.method_8326();
        int originZ = chunkPos.method_8328();
        int minY = Math.max(this.mc.field_1687.method_31607(), -64);
        int maxY = Math.min(minY + this.mc.field_1687.method_31605() - 1, 0);
        class_2338.class_2339 cursor = new class_2338.class_2339();
        for (int x = originX; x < originX + 16; ++x) {
            for (int z = originZ; z < originZ + 16; ++z) {
                for (int y = minY; y < maxY; ++y) {
                    cursor.method_10103(x, y, z);
                    class_2680 state = chunk.method_8320((class_2338)cursor);
                    if (PlayerChunkFinder.isRotatedDeepslate(state)) {
                        return "MODIFIED_DEEPSLATE";
                    }
                    if (!((Boolean)this.detectUnnatural.get()).booleanValue() || !PlayerChunkFinder.isUnnaturalUndergroundBlock(state.method_26204())) continue;
                    return "UNNATURAL";
                }
            }
        }
        return null;
    }

    private static boolean isRotatedDeepslate(class_2680 state) {
        if (state.method_26204() != class_2246.field_28888) {
            return false;
        }
        if (!state.method_28498((class_2769)class_2741.field_12496)) {
            return false;
        }
        return state.method_11654((class_2769)class_2741.field_12496) != class_2350.class_2351.field_11052;
    }

    private static boolean isUnnaturalUndergroundBlock(class_2248 b) {
        return b == class_2246.field_10445 || b == class_2246.field_29031 || b == class_2246.field_10161 || b == class_2246.field_9975 || b == class_2246.field_10148 || b == class_2246.field_10334 || b == class_2246.field_10218 || b == class_2246.field_10075 || b == class_2246.field_37577 || b == class_2246.field_42751 || b == class_2246.field_22126 || b == class_2246.field_22127 || b == class_2246.field_10336 || b == class_2246.field_10099 || b == class_2246.field_9983 || b == class_2246.field_10167 || b == class_2246.field_9980 || b == class_2246.field_10181 || b == class_2246.field_16333 || b == class_2246.field_16334 || b == class_2246.field_10034 || b == class_2246.field_10380 || b == class_2246.field_16328 || b == class_2246.field_10033;
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (this.mc.field_1724 == null || this.modifiedChunks.isEmpty()) {
            return;
        }
        int radius = (Integer)this.renderRadius.get();
        int pcx = this.mc.field_1724.method_31476().field_9181;
        int pcz = this.mc.field_1724.method_31476().field_9180;
        double y = (Double)this.renderY.get();
        for (class_1923 pos : this.modifiedChunks.keySet()) {
            if (Math.abs(pos.field_9181 - pcx) > radius || Math.abs(pos.field_9180 - pcz) > radius) continue;
            double x1 = pos.method_8326();
            double z1 = pos.method_8328();
            if (((Boolean)this.filledEsp.get()).booleanValue()) {
                event.renderer.box(x1, y, z1, x1 + 16.0, y + 0.12, z1 + 16.0, (Color)this.fillColor.get(), (Color)this.lineColor.get(), ShapeMode.Both, 0);
                continue;
            }
            event.renderer.box(x1, y, z1, x1 + 16.0, y + 0.12, z1 + 16.0, new Color(0, 0, 0, 0), (Color)this.lineColor.get(), ShapeMode.Lines, 0);
        }
    }
}

