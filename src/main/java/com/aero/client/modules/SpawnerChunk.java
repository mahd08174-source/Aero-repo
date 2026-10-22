package com.aero.client.modules;

import com.aero.client.AeroCategory;
import java.awt.Color;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.aero.client.core.events.Render3DEvent;
import com.aero.client.core.events.TickEvent;
import com.aero.client.core.render.ShapeMode;
import com.aero.client.core.settings.BoolSetting;
import com.aero.client.core.settings.ColorSetting;
import com.aero.client.core.settings.DoubleSetting;
import com.aero.client.core.settings.IntSetting;
import com.aero.client.core.settings.Setting;
import com.aero.client.core.settings.SettingGroup;
import com.aero.client.core.Module;
import com.aero.client.core.render.SettingColor;
import com.aero.client.core.events.EventHandler;
import net.minecraft.class_1923;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2636;
import net.minecraft.class_2818;
import net.minecraft.class_370;
import net.minecraft.class_374;

public class SpawnerChunk
extends Module {
    private final SettingGroup sgSpawners;
    private final SettingGroup sgTracers;
    private final Setting<Boolean> spawnerChatNotify;
    private final Setting<Boolean> spawnerToastNotify;
    private final Setting<Boolean> highlightSpawnerChunks;
    private final Setting<Boolean> renderSpawnerBoxes;
    private final Setting<Double> spawnerRainbowSpeed;
    private final Setting<Integer> spawnerRainbowAlpha;
    private final Setting<SettingColor> spawnerChunkLineColor;
    private final Setting<SettingColor> spawnerChunkFillColor;
    private final Setting<Boolean> showSpawnerTracers;
    private final Setting<SettingColor> spawnerTracerColor;
    private final Set<class_2338> foundSpawners;
    private float hue;

    public SpawnerChunk() {
        super(AeroCategory.CATEGORY, "spawner-chunk", "Finds chunks containing mob spawners.");
        this.sgSpawners = this.settings.getDefaultGroup();
        this.sgTracers = this.settings.createGroup("Tracers");
        this.spawnerChatNotify = this.sgSpawners.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("spawner-chat-notify")).description("Send a chat message when a new mob spawner is found.")).defaultValue(false)).build());
        this.spawnerToastNotify = this.sgSpawners.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("spawner-toast-notify")).description("Show a toast notification when a new mob spawner is found.")).defaultValue(false)).build());
        this.highlightSpawnerChunks = this.sgSpawners.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("highlight-spawner-chunks")).description("Highlight the chunk column for chunks containing spawners.")).defaultValue(true)).build());
        this.renderSpawnerBoxes = this.sgSpawners.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render-spawner-boxes")).description("Render rainbow boxes around mob spawners.")).defaultValue(true)).build());
        this.spawnerRainbowSpeed = this.sgSpawners.add((Setting)((DoubleSetting.Builder)new DoubleSetting.Builder().name("spawner-rainbow-speed")).defaultValue(2.0).sliderMin(0.1).sliderMax(10.0).build());
        this.spawnerRainbowAlpha = this.sgSpawners.add((Setting)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("spawner-rainbow-alpha")).defaultValue(255)).sliderMin(0).sliderMax(255).build());
        this.spawnerChunkLineColor = this.sgSpawners.add((Setting)((ColorSetting.Builder)new ColorSetting.Builder().name("spawner-chunk-line-color")).defaultValue(new SettingColor(255, 165, 0, 255)).build());
        this.spawnerChunkFillColor = this.sgSpawners.add((Setting)((ColorSetting.Builder)new ColorSetting.Builder().name("spawner-chunk-fill-color")).defaultValue(new SettingColor(255, 165, 0, 30)).build());
        this.showSpawnerTracers = this.sgTracers.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("show-spawner-tracers")).defaultValue(false)).build());
        this.spawnerTracerColor = this.sgTracers.add((Setting)((ColorSetting.Builder)new ColorSetting.Builder().name("spawner-tracer-color")).defaultValue(new SettingColor(255, 165, 0, 200)).build());
        this.foundSpawners = new HashSet<class_2338>();
        this.hue = 0.0f;
    }

    public void onActivate() {
        this.foundSpawners.clear();
    }

    public void onDeactivate() {
        this.foundSpawners.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (this.mc.field_1687 == null || this.mc.field_1724 == null) {
            return;
        }
        int range = 8;
        class_1923 pc = this.mc.field_1724.method_31476();
        for (int dx = -range; dx <= range; ++dx) {
            for (int dz = -range; dz <= range; ++dz) {
                class_2818 chunk = this.mc.field_1687.method_8497(pc.field_9181 + dx, pc.field_9180 + dz);
                if (chunk == null) continue;
                for (Map.Entry entry : chunk.method_12214().entrySet()) {
                    class_2338 pos;
                    if (!(entry.getValue() instanceof class_2636) || this.foundSpawners.contains(pos = (class_2338)entry.getKey())) continue;
                    this.foundSpawners.add(pos);
                    String msg = "Spawner at X" + pos.method_10263() + " Y" + pos.method_10264() + " Z" + pos.method_10260();
                    if (((Boolean)this.spawnerChatNotify.get()).booleanValue()) {
                        this.mc.field_1724.method_7353((class_2561)class_2561.method_43470((String)("\u00a76[SpawnerChunk] \u00a7f" + msg)), false);
                    }
                    if (!((Boolean)this.spawnerToastNotify.get()).booleanValue()) continue;
                    class_370.method_27024((class_374)this.mc.method_1566(), (class_370.class_9037)class_370.class_9037.field_47588, (class_2561)class_2561.method_43470((String)"Spawner Found"), (class_2561)class_2561.method_43470((String)msg));
                }
            }
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (this.mc.field_1687 == null || this.mc.field_1724 == null) {
            return;
        }
        this.hue = (this.hue + (float)((Double)this.spawnerRainbowSpeed.get() * 0.005)) % 1.0f;
        int rgb = Color.HSBtoRGB(this.hue, 1.0f, 1.0f);
        SettingColor rainbow = new SettingColor(rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF, ((Integer)this.spawnerRainbowAlpha.get()).intValue());
        class_243 cam = this.mc.field_1724.method_5836(1.0f);
        HashSet<class_1923> renderedChunks = new HashSet<class_1923>();
        for (class_2338 pos : this.foundSpawners) {
            class_1923 cp;
            if (((Boolean)this.renderSpawnerBoxes.get()).booleanValue()) {
                event.renderer.box((double)pos.method_10263(), (double)pos.method_10264(), (double)pos.method_10260(), (double)(pos.method_10263() + 1), (double)(pos.method_10264() + 1), (double)(pos.method_10260() + 1), (com.aero.client.core.render.Color)new SettingColor(0, 0, 0, 0), (com.aero.client.core.render.Color)rainbow, ShapeMode.Lines, 0);
            }
            if (((Boolean)this.showSpawnerTracers.get()).booleanValue()) {
                event.renderer.line(cam.field_1352, cam.field_1351, cam.field_1350, (double)pos.method_10263() + 0.5, (double)pos.method_10264() + 0.5, (double)pos.method_10260() + 0.5, (com.aero.client.core.render.Color)this.spawnerTracerColor.get());
            }
            if (!((Boolean)this.highlightSpawnerChunks.get()).booleanValue() || !renderedChunks.add(cp = new class_1923(pos))) continue;
            event.renderer.box((double)(cp.field_9181 * 16), -64.0, (double)(cp.field_9180 * 16), (double)(cp.field_9181 * 16 + 16), 320.0, (double)(cp.field_9180 * 16 + 16), (com.aero.client.core.render.Color)this.spawnerChunkFillColor.get(), (com.aero.client.core.render.Color)this.spawnerChunkLineColor.get(), ShapeMode.Both, 0);
        }
    }
}

