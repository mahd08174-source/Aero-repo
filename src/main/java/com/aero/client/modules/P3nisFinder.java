package com.aero.client.modules;

import com.aero.client.AeroCategory;
import java.util.LinkedHashMap;
import java.util.Map;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
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
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2818;
import net.minecraft.class_370;
import net.minecraft.class_374;

public class P3nisFinder
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgChunkDensity;
    private final SettingGroup sgTracers;
    private final Setting<Double> scanRange;
    private final Setting<Integer> scanIntervalTicks;
    private final Setting<Boolean> renderDenseChunkMarkers;
    private final Setting<Integer> denseChunkThreshold;
    private final Setting<Integer> maxDenseChunkMarkers;
    private final Setting<Integer> maxSavedDenseChunks;
    private final Setting<Double> denseChunkMarkerSize;
    private final Setting<Double> denseChunkMarkerThickness;
    private final Setting<Double> markerHeightOffset;
    private final Setting<Boolean> denseChunkChatNotify;
    private final Setting<Boolean> denseChunkToastNotify;
    private final Setting<SettingColor> denseChunkLineColor;
    private final Setting<SettingColor> denseChunkFillColor;
    private final Setting<Boolean> showDenseChunkTracers;
    private final Setting<SettingColor> denseChunkTracerColor;
    private final Map<class_1923, Double> denseChunks;
    private int tickCounter;

    public P3nisFinder() {
        super(AeroCategory.CATEGORY, "p3nis-finder", "Scans loaded chunks for high block-entity density. Chests, shulkers, and hoppers indicate player storage \u2014 a reliable base-finding method.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgChunkDensity = this.settings.createGroup("Chunk Density");
        this.sgTracers = this.settings.createGroup("Tracers");
        this.scanRange = this.sgGeneral.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("scan-range")).description("Horizontal block range to scan loaded chunks.")).defaultValue(64.0).sliderMin(16.0).sliderMax(256.0).build());
        this.scanIntervalTicks = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("scan-interval-ticks")).description("How often to rescan. Higher = better performance.")).defaultValue(5)).sliderMin(1).sliderMax(40).build());
        this.renderDenseChunkMarkers = this.sgChunkDensity.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render-dense-chunk-markers")).description("Render a flat marker for chunks with too many block entities.")).defaultValue(true)).build());
        this.denseChunkThreshold = this.sgChunkDensity.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("dense-chunk-threshold")).description("Chunks with more block entities than this get a marker.")).defaultValue(22)).sliderMin(1).sliderMax(256).build());
        this.maxDenseChunkMarkers = this.sgChunkDensity.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("max-dense-chunk-markers")).description("Max dense chunk markers rendered at once.")).defaultValue(64)).sliderMin(1).sliderMax(256).build());
        this.maxSavedDenseChunks = this.sgChunkDensity.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("max-saved-dense-chunks")).description("Max dense chunk detections to keep. Oldest removed first.")).defaultValue(256)).sliderMin(1).sliderMax(1024).build());
        this.denseChunkMarkerSize = this.sgChunkDensity.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("dense-chunk-marker-size")).description("Size of the dense chunk highlight. 16 = full chunk.")).defaultValue(20.0).sliderMin(1.0).sliderMax(32.0).build());
        this.denseChunkMarkerThickness = this.sgChunkDensity.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("dense-chunk-marker-thickness")).description("Thickness of the dense chunk highlight layer.")).defaultValue(0.08).sliderMin(0.01).sliderMax(1.0).build());
        this.markerHeightOffset = this.sgChunkDensity.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("marker-height-offset")).description("Fixed Y offset above sea level for the marker.")).defaultValue(0.05).sliderMin(0.0).sliderMax(5.0).build());
        this.denseChunkChatNotify = this.sgChunkDensity.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("chat-notify")).description("Post a chat message when a new dense chunk is detected.")).defaultValue(false)).build());
        this.denseChunkToastNotify = this.sgChunkDensity.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("toast-notify")).description("Show a toast when a new dense chunk is detected.")).defaultValue(false)).build());
        this.denseChunkLineColor = this.sgChunkDensity.add((Setting)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("dense-chunk-line-color")).description("Outline color for dense chunk markers.")).defaultValue(new SettingColor(255, 0, 0, 255)).build());
        this.denseChunkFillColor = this.sgChunkDensity.add((Setting)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("dense-chunk-fill-color")).description("Fill color for dense chunk markers.")).defaultValue(new SettingColor(255, 0, 0, 50)).build());
        this.showDenseChunkTracers = this.sgTracers.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("show-dense-chunk-tracers")).description("Draw tracer lines to saved dense chunk detections.")).defaultValue(false)).build());
        this.denseChunkTracerColor = this.sgTracers.add((Setting)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("dense-chunk-tracer-color")).description("Tracer color for dense chunk detections.")).defaultValue(new SettingColor(255, 0, 0, 200)).build());
        this.denseChunks = new LinkedHashMap<class_1923, Double>();
        this.tickCounter = 0;
    }

    public void onActivate() {
        this.denseChunks.clear();
        this.tickCounter = 0;
    }

    public void onDeactivate() {
        this.denseChunks.clear();
    }

    private void sendChat(String msg) {
        if (this.mc.field_1724 == null) {
            return;
        }
        this.mc.field_1724.method_7353((class_2561)class_2561.method_43470((String)("\u00a75[P3nis Finder] \u00a7f" + msg)), false);
    }

    private void sendToast(String desc) {
        if (this.mc.method_1566() == null) {
            return;
        }
        class_370.method_27024((class_374)this.mc.method_1566(), (class_370.class_9037)class_370.class_9037.field_47588, (class_2561)class_2561.method_43470((String)"P3nis Finder"), (class_2561)class_2561.method_43470((String)desc));
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (this.mc.field_1687 == null || this.mc.field_1724 == null) {
            return;
        }
        if (++this.tickCounter < (Integer)this.scanIntervalTicks.get()) {
            return;
        }
        this.tickCounter = 0;
        int range = (int)((Double)this.scanRange.get() / 16.0);
        class_1923 playerChunk = this.mc.field_1724.method_31476();
        for (int dx = -range; dx <= range; ++dx) {
            for (int dz = -range; dz <= range; ++dz) {
                int count;
                class_2818 chunk;
                class_1923 cp = new class_1923(playerChunk.field_9181 + dx, playerChunk.field_9180 + dz);
                if (this.denseChunks.containsKey(cp) || (chunk = this.mc.field_1687.method_8497(cp.field_9181, cp.field_9180)) == null || (count = chunk.method_12214().size()) < (Integer)this.denseChunkThreshold.get()) continue;
                if (this.denseChunks.size() >= (Integer)this.maxSavedDenseChunks.get()) {
                    this.denseChunks.remove(this.denseChunks.keySet().iterator().next());
                }
                double fixedY = this.mc.field_1724.method_23318() + (Double)this.markerHeightOffset.get();
                this.denseChunks.put(cp, fixedY);
                String msg = "Dense chunk at " + cp.field_9181 + ", " + cp.field_9180 + " | " + count + " block entities";
                if (((Boolean)this.denseChunkChatNotify.get()).booleanValue()) {
                    this.sendChat(msg);
                }
                if (!((Boolean)this.denseChunkToastNotify.get()).booleanValue()) continue;
                this.sendToast(msg);
            }
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (this.mc.field_1687 == null || this.mc.field_1724 == null) {
            return;
        }
        if (!((Boolean)this.renderDenseChunkMarkers.get()).booleanValue() && !((Boolean)this.showDenseChunkTracers.get()).booleanValue()) {
            return;
        }
        class_243 cam = this.mc.field_1724.method_5836(1.0f);
        int rendered = 0;
        for (Map.Entry<class_1923, Double> entry : this.denseChunks.entrySet()) {
            if (rendered++ >= (Integer)this.maxDenseChunkMarkers.get()) break;
            class_1923 cp = entry.getKey();
            double y = entry.getValue();
            double s = (Double)this.denseChunkMarkerSize.get();
            double x1 = (double)(cp.field_9181 * 16 + 8) - s / 2.0;
            double z1 = (double)(cp.field_9180 * 16 + 8) - s / 2.0;
            double cx = x1 + s / 2.0;
            double cz = z1 + s / 2.0;
            if (((Boolean)this.renderDenseChunkMarkers.get()).booleanValue()) {
                event.renderer.box(x1, y, z1, x1 + s, y + (Double)this.denseChunkMarkerThickness.get(), z1 + s, (Color)this.denseChunkFillColor.get(), (Color)this.denseChunkLineColor.get(), ShapeMode.Both, 0);
            }
            if (!((Boolean)this.showDenseChunkTracers.get()).booleanValue()) continue;
            event.renderer.line(cam.field_1352, cam.field_1351, cam.field_1350, cx, y, cz, (Color)this.denseChunkTracerColor.get());
        }
    }
}

