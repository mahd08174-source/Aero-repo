package com.aero.client.modules;

import com.aero.client.AeroCategory;
import java.util.HashMap;
import java.util.Map;
import com.aero.client.api.Render3DEvent;
import com.aero.client.api.TickEvent;
import com.aero.client.api.ShapeMode;
import com.aero.client.api.BoolSetting;
import com.aero.client.api.ColorSetting;
import com.aero.client.api.IntSetting;
import com.aero.client.api.Setting;
import com.aero.client.api.SettingGroup;
import com.aero.client.api.Module;
import com.aero.client.api.Color;
import com.aero.client.api.SettingColor;
import com.aero.client.api.EventHandler;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.text.Text;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.FurnaceBlockEntity;
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
    private final Map<ChunkPos, Integer> chunkScores;
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
        this.chunkScores = new HashMap<ChunkPos, Integer>();
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
        if (this.mc.world == null || this.mc.player == null) {
            return;
        }
        if (++this.tickCounter < 40) {
            return;
        }
        this.tickCounter = 0;
        ChunkPos pc = this.mc.player.getChunkPos();
        int r = (Integer)this.scanRadius.get();
        for (int dx = -r; dx <= r; ++dx) {
            for (int dz = -r; dz <= r; ++dz) {
                ChunkPos cp = new ChunkPos(pc.x + dx, pc.z + dz);
                WorldChunk chunk = this.mc.world.getChunk(cp.x, cp.z);
                if (chunk == null) continue;
                int score = 0;
                for (Map.Entry e : chunk.getBlockEntities().entrySet()) {
                    BlockEntity be = (BlockEntity)e.getValue();
                    if (be instanceof ChestBlockEntity) {
                        score += 3;
                        continue;
                    }
                    if (be instanceof ShulkerBoxBlockEntity) {
                        score += 5;
                        continue;
                    }
                    if (be instanceof BarrelBlockEntity) {
                        score += 3;
                        continue;
                    }
                    if (be instanceof HopperBlockEntity) {
                        ++score;
                        continue;
                    }
                    if (!(be instanceof FurnaceBlockEntity)) continue;
                    ++score;
                }
                if (score < (Integer)this.suspicionThreshold.get()) continue;
                Integer prev = this.chunkScores.get(cp);
                this.chunkScores.put(cp, score);
                if (prev != null && prev >= (Integer)this.suspicionThreshold.get() || !((Boolean)this.logToChat.get()).booleanValue()) continue;
                this.mc.player.sendMessage((Text)Text.literal((String)("\u00a7c[StorageScanner] \u00a7fSuspicious storage at \u00a7b" + cp.x + ", " + cp.z + " \u00a77(score: " + score + ")")), false);
            }
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (!((Boolean)this.renderSuspicious.get()).booleanValue() || this.chunkScores.isEmpty()) {
            return;
        }
        double py = this.mc.player != null ? this.mc.player.getY() : 64.0;
        for (Map.Entry<ChunkPos, Integer> e : this.chunkScores.entrySet()) {
            ChunkPos cp = e.getKey();
            SettingColor color = e.getValue() > (Integer)this.suspicionThreshold.get() * 2 ? (SettingColor)this.highSuspicionColor.get() : (SettingColor)this.lowSuspicionColor.get();
            event.renderer.box((double)(cp.x * 16), py - 2.0, (double)(cp.z * 16), (double)(cp.x * 16 + 16), py + 0.1, (double)(cp.z * 16 + 16), (Color)color, (Color)color, ShapeMode.Both, 0);
        }
    }
}

