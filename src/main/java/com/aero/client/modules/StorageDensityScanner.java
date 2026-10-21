package com.aero.client.modules;

import com.aero.client.AeroCategory;
import java.util.HashMap;
import java.util.Map;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1923;
import net.minecraft.class_2561;
import net.minecraft.class_2586;
import net.minecraft.class_2595;
import net.minecraft.class_2614;
import net.minecraft.class_2627;
import net.minecraft.class_2818;
import net.minecraft.class_3719;
import net.minecraft.class_3866;

public class StorageDensityScanner
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgRender;
    private final Setting<Integer> scanRadius;
    private final Setting<Integer> suspicionThreshold;
    private final Setting<Boolean> logToChat;
    private final Setting<Boolean> renderSuspicious;
    private final Setting<SettingColor> highSuspicionColor;
    private final Setting<SettingColor> lowSuspicionColor;
    private final Map<class_1923, Integer> chunkScores;
    private int tickCounter;

    public StorageDensityScanner() {
        super(AeroCategory.CATEGORY, "storage-density-scanner", "Scans nearby chunks for high storage density (chests, shulkers, barrels). Suspicious areas are highlighted and optionally announced in chat.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgRender = this.settings.createGroup("Render");
        this.scanRadius = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("scan-radius")).description("Chunk radius to scan around you for storage blocks.")).defaultValue(4)).sliderMin(1).sliderMax(16).build());
        this.suspicionThreshold = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("suspicion-threshold")).description("Minimum suspicion score to flag a chunk. Chests: +3, shulkers: +5, barrels: +3, hoppers/furnaces: +1.")).defaultValue(5)).sliderMin(1).sliderMax(50).build());
        this.logToChat = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("chat-alert")).description("Send a chat message when a suspicious storage chunk is first detected.")).defaultValue(true)).build());
        this.renderSuspicious = this.sgRender.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render")).description("Render colored highlights over suspicious chunks.")).defaultValue(true)).build());
        this.highSuspicionColor = this.sgRender.add((Setting)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("high-suspicion-color")).description("Highlight color for chunks with very high suspicion scores (2x threshold).")).defaultValue(new SettingColor(255, 0, 0, 80)).build());
        this.lowSuspicionColor = this.sgRender.add((Setting)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("low-suspicion-color")).description("Highlight color for chunks that just met the threshold.")).defaultValue(new SettingColor(255, 165, 0, 50)).build());
        this.chunkScores = new HashMap<class_1923, Integer>();
        this.tickCounter = 0;
    }

    public void onActivate() {
        this.chunkScores.clear();
        this.tickCounter = 0;
    }

    public void onDeactivate() {
        this.chunkScores.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (this.mc.field_1687 == null || this.mc.field_1724 == null) {
            return;
        }
        if (++this.tickCounter < 40) {
            return;
        }
        this.tickCounter = 0;
        class_1923 pc = this.mc.field_1724.method_31476();
        int r = (Integer)this.scanRadius.get();
        for (int dx = -r; dx <= r; ++dx) {
            for (int dz = -r; dz <= r; ++dz) {
                class_1923 cp = new class_1923(pc.field_9181 + dx, pc.field_9180 + dz);
                class_2818 chunk = this.mc.field_1687.method_8497(cp.field_9181, cp.field_9180);
                if (chunk == null) continue;
                int score = 0;
                for (Map.Entry e : chunk.method_12214().entrySet()) {
                    class_2586 be = (class_2586)e.getValue();
                    if (be instanceof class_2595) {
                        score += 3;
                        continue;
                    }
                    if (be instanceof class_2627) {
                        score += 5;
                        continue;
                    }
                    if (be instanceof class_3719) {
                        score += 3;
                        continue;
                    }
                    if (be instanceof class_2614) {
                        ++score;
                        continue;
                    }
                    if (!(be instanceof class_3866)) continue;
                    ++score;
                }
                if (score < (Integer)this.suspicionThreshold.get()) continue;
                Integer prev = this.chunkScores.get(cp);
                this.chunkScores.put(cp, score);
                if (prev != null && prev >= (Integer)this.suspicionThreshold.get() || !((Boolean)this.logToChat.get()).booleanValue()) continue;
                this.mc.field_1724.method_7353((class_2561)class_2561.method_43470((String)("\u00a7c[StorageScanner] \u00a7fSuspicious storage at \u00a7b" + cp.field_9181 + ", " + cp.field_9180 + " \u00a77(score: " + score + ")")), false);
            }
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (!((Boolean)this.renderSuspicious.get()).booleanValue() || this.chunkScores.isEmpty()) {
            return;
        }
        double py = this.mc.field_1724 != null ? this.mc.field_1724.method_23318() : 64.0;
        for (Map.Entry<class_1923, Integer> e : this.chunkScores.entrySet()) {
            class_1923 cp = e.getKey();
            SettingColor color = e.getValue() > (Integer)this.suspicionThreshold.get() * 2 ? (SettingColor)this.highSuspicionColor.get() : (SettingColor)this.lowSuspicionColor.get();
            event.renderer.box((double)(cp.field_9181 * 16), py - 2.0, (double)(cp.field_9180 * 16), (double)(cp.field_9181 * 16 + 16), py + 0.1, (double)(cp.field_9180 * 16 + 16), (Color)color, (Color)color, ShapeMode.Both, 0);
        }
    }
}

