package com.aero.client.modules;

import com.aero.client.AeroCategory;
import java.awt.Color;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.aero.client.api.Render3DEvent;
import com.aero.client.api.TickEvent;
import com.aero.client.api.ShapeMode;
import com.aero.client.api.BoolSetting;
import com.aero.client.api.ColorSetting;
import com.aero.client.api.DoubleSetting;
import com.aero.client.api.IntSetting;
import com.aero.client.api.Setting;
import com.aero.client.api.SettingGroup;
import com.aero.client.api.Module;
import com.aero.client.api.SettingColor;
import com.aero.client.api.EventHandler;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.Text;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.ToastManager;
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
    private final Set<BlockPos> foundSpawners;
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
        this.foundSpawners = new HashSet<BlockPos>();
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
        if (this.mc.world == null || this.mc.player == null) {
            return;
        }
        int range = 8;
        ChunkPos pc = this.mc.player.getChunkPos();
        for (int dx = -range; dx <= range; ++dx) {
            for (int dz = -range; dz <= range; ++dz) {
                WorldChunk chunk = this.mc.world.getChunk(pc.x + dx, pc.z + dz);
                if (chunk == null) continue;
                for (Map.Entry entry : chunk.getBlockEntities().entrySet()) {
                    BlockPos pos;
                    if (!(entry.getValue() instanceof MobSpawnerBlockEntity) || this.foundSpawners.contains(pos = (BlockPos)entry.getKey())) continue;
                    this.foundSpawners.add(pos);
                    String msg = "Spawner at X" + pos.getX() + " Y" + pos.getY() + " Z" + pos.getZ();
                    if (((Boolean)this.spawnerChatNotify.get()).booleanValue()) {
                        this.mc.player.sendMessage((Text)Text.literal((String)("\u00a76[SpawnerChunk] \u00a7f" + msg)), false);
                    }
                    if (!((Boolean)this.spawnerToastNotify.get()).booleanValue()) continue;
                    SystemToast.add((ToastManager)this.mc.getToastManager(), (SystemToast.Type)SystemToast.Type.PERIODIC_NOTIFICATION, (Text)Text.literal((String)"Spawner Found"), (Text)Text.literal((String)msg));
                }
            }
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (this.mc.world == null || this.mc.player == null) {
            return;
        }
        this.hue = (this.hue + (float)((Double)this.spawnerRainbowSpeed.get() * 0.005)) % 1.0f;
        int rgb = Color.HSBtoRGB(this.hue, 1.0f, 1.0f);
        SettingColor rainbow = new SettingColor(rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF, ((Integer)this.spawnerRainbowAlpha.get()).intValue());
        Vec3d cam = this.mc.player.getCameraPosVec(1.0f);
        HashSet<ChunkPos> renderedChunks = new HashSet<ChunkPos>();
        for (BlockPos pos : this.foundSpawners) {
            ChunkPos cp;
            if (((Boolean)this.renderSpawnerBoxes.get()).booleanValue()) {
                event.renderer.box((double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), (double)(pos.getX() + 1), (double)(pos.getY() + 1), (double)(pos.getZ() + 1), (com.aero.client.api.Color)new SettingColor(0, 0, 0, 0), (com.aero.client.api.Color)rainbow, ShapeMode.Lines, 0);
            }
            if (((Boolean)this.showSpawnerTracers.get()).booleanValue()) {
                event.renderer.line(cam.x, cam.y, cam.z, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, (com.aero.client.api.Color)this.spawnerTracerColor.get());
            }
            if (!((Boolean)this.highlightSpawnerChunks.get()).booleanValue() || !renderedChunks.add(cp = new ChunkPos(pos))) continue;
            event.renderer.box((double)(cp.x * 16), -64.0, (double)(cp.z * 16), (double)(cp.x * 16 + 16), 320.0, (double)(cp.z * 16 + 16), (com.aero.client.api.Color)this.spawnerChunkFillColor.get(), (com.aero.client.api.Color)this.spawnerChunkLineColor.get(), ShapeMode.Both, 0);
        }
    }
}

