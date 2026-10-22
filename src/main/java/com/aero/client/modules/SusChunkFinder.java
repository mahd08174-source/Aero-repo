package com.aero.client.modules;

import com.aero.client.AeroCategory;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.world.chunk.WorldChunk;
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
    public final Map<ChunkPos, ChunkHit> hits;
    public final Set<ChunkPos> scanned;
    public final Set<ChunkPos> queued;
    public final ArrayDeque<ChunkPos> scanQueue;
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
        this.hits = new ConcurrentHashMap<ChunkPos, ChunkHit>();
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
        ChunkPos pos;
        if (this.mc.world == null || this.mc.player == null) {
            return;
        }
        if (++this.rescanTimer >= 400) {
            this.cleanUpFarHits();
            this.rescanTimer = 0;
        }
        this.enqueueChunks();
        for (int n = 0; n < 3 && (pos = this.scanQueue.poll()) != null; ++n) {
            this.queued.remove(pos);
            WorldChunk chunk = this.mc.world.getChunk(pos.x, pos.z);
            if (chunk == null || chunk.isEmpty() || !this.scanned.add(pos)) continue;
            this.scanChunk(chunk, pos);
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (this.hits.isEmpty()) {
            return;
        }
        for (ChunkPos pos : this.hits.keySet()) {
            double x1 = pos.getStartX();
            double z1 = pos.getStartZ();
            event.renderer.box(x1, 63.0, z1, x1 + 16.0, 63.15, z1 + 16.0, (Color)this.fillColor.get(), (Color)this.lineColor.get(), ShapeMode.Both, 0);
        }
    }

    public void enqueueChunks() {
        ChunkPos center = this.mc.player.getChunkPos();
        int radius = Math.min((Integer)this.simDistance.get(), this.viewDist());
        for (int dx = -radius; dx <= radius; ++dx) {
            for (int dz = -radius; dz <= radius; ++dz) {
                ChunkPos pos = new ChunkPos(center.x + dx, center.z + dz);
                if (this.scanned.contains(pos) || !this.queued.add(pos)) continue;
                this.scanQueue.offer(pos);
            }
        }
    }

    public void cleanUpFarHits() {
        if (this.mc.player == null) {
            return;
        }
        ChunkPos center = this.mc.player.getChunkPos();
        int maxDist = Math.max((Integer)this.simDistance.get(), this.viewDist()) + 4;
        this.hits.keySet().removeIf(p -> Math.abs(p.x - center.x) > maxDist || Math.abs(p.z - center.z) > maxDist);
        this.scanned.removeIf(p -> Math.abs(p.x - center.x) > maxDist || Math.abs(p.z - center.z) > maxDist);
        this.queued.removeIf(p -> Math.abs(p.x - center.x) > maxDist || Math.abs(p.z - center.z) > maxDist);
    }

    public void scanChunk(WorldChunk chunk, ChunkPos chunkPos) {
        BlockPos.Mutable cursor = new BlockPos.Mutable();
        BlockPos.Mutable amCursor = new BlockPos.Mutable();
        int originX = chunkPos.getStartX();
        int originZ = chunkPos.getStartZ();
        int minY = this.mc.world.getBottomY();
        int maxY = minY + this.mc.world.getHeight();
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
                    BlockState state;
                    cursor.set(originX + dx, y, originZ + dz);
                    try {
                        state = chunk.getBlockState((BlockPos)cursor);
                    }
                    catch (Exception e) {
                        continue;
                    }
                    Block block = state.getBlock();
                    if (block == Blocks.KELP || block == Blocks.KELP_PLANT) {
                        if (++kr > kb) {
                            kb = kr;
                        }
                    } else {
                        kr = 0;
                    }
                    if (block == Blocks.CAVE_VINES || block == Blocks.CAVE_VINES_PLANT) {
                        if (++cvr > cvb) {
                            cvb = cvr;
                        }
                    } else {
                        cvr = 0;
                    }
                    if (block == Blocks.VINE) {
                        if (++vr > vb) {
                            vb = vr;
                        }
                    } else {
                        vr = 0;
                    }
                    if (block == Blocks.SUGAR_CANE) {
                        if (++scr > scb) {
                            scb = scr;
                        }
                    } else {
                        scr = 0;
                    }
                    if (block == Blocks.SWEET_BERRY_BUSH && state.contains((Property)Properties.AGE_3)) {
                        try {
                            if ((Integer)state.get((Property)Properties.AGE_3) >= 3) {
                                ++ripeBerries;
                            }
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                    if (block == Blocks.COCOA) {
                        ++cocoaCount;
                    }
                    if (block != Blocks.BUDDING_AMETHYST || this.hasAmethystGrowthOnAnyFace(amCursor, originX + dx, y, originZ + dz)) continue;
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

    public boolean hasAmethystGrowthOnAnyFace(BlockPos.Mutable cursor, int x, int y, int z) {
        int[][] offsets;
        for (int[] off : offsets = new int[][]{{0, 1, 0}, {0, -1, 0}, {1, 0, 0}, {-1, 0, 0}, {0, 0, 1}, {0, 0, -1}}) {
            cursor.set(x + off[0], y + off[1], z + off[2]);
            Block b = this.mc.world.getBlockState((BlockPos)cursor).getBlock();
            if (b != Blocks.AMETHYST_CLUSTER && b != Blocks.LARGE_AMETHYST_BUD && b != Blocks.MEDIUM_AMETHYST_BUD && b != Blocks.SMALL_AMETHYST_BUD) continue;
            return true;
        }
        return false;
    }

    public int viewDist() {
        return this.mc.options == null ? 8 : (Integer)this.mc.options.getViewDistance().getValue();
    }

    public void clear() {
        this.hits.clear();
        this.scanned.clear();
        this.queued.clear();
        this.scanQueue.clear();
        this.rescanTimer = 0;
    }

    public record ChunkHit(ChunkPos pos, String reason) {
    }
}

