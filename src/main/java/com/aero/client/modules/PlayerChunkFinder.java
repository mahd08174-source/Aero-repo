package com.aero.client.modules;

import com.aero.client.AeroCategory;
import java.util.LinkedHashMap;
import java.util.Map;
import com.aero.client.api.Render3DEvent;
import com.aero.client.api.ChunkDataEvent;
import com.aero.client.api.ShapeMode;
import com.aero.client.api.BoolSetting;
import com.aero.client.api.ColorSetting;
import com.aero.client.api.DoubleSetting;
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
import net.minecraft.util.math.Direction;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.world.chunk.WorldChunk;
public class PlayerChunkFinder
extends Module {
    private final SettingGroup sg;
    public final Setting<Boolean> detectUnnatural;
    public final Setting<Integer> renderRadius;
    public final Setting<Double> renderY;
    public final Setting<Boolean> filledEsp;
    public final Setting<SettingColor> fillColor;
    public final Setting<SettingColor> lineColor;
    private final Map<ChunkPos, String> modifiedChunks;

    public PlayerChunkFinder() {
        super(AeroCategory.CATEGORY, "player-chunk-finder", "Detects underground player-modified chunks via incoming chunk packets.");
        this.sg = this.settings.getDefaultGroup();
        this.detectUnnatural = this.sg.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("detect-unnatural")).description("Detect cobblestone, planks, torches, rails, containers underground.")).defaultValue(true)).build());
        this.renderRadius = this.sg.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("render-radius")).description("Render grid radius in chunks.")).defaultValue(8)).min(1).sliderMax(32).build());
        this.renderY = this.sg.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("render-y")).description("Y level to draw the slab at.")).defaultValue(-60.0).min(-64.0).max(320.0).sliderRange(-64.0, 320.0).build());
        this.filledEsp = this.sg.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("filled-esp")).defaultValue(true)).build());
        this.fillColor = this.sg.add((Setting)((ColorSetting.Builder)new ColorSetting.Builder().name("fill-color")).defaultValue(new SettingColor(255, 82, 82, 80)).build());
        this.lineColor = this.sg.add((Setting)((ColorSetting.Builder)new ColorSetting.Builder().name("line-color")).defaultValue(new SettingColor(255, 82, 82, 255)).build());
        this.modifiedChunks = new LinkedHashMap<ChunkPos, String>();
    }

    public void onActivate() {
        this.modifiedChunks.clear();
    }

    public void onDeactivate() {
        this.modifiedChunks.clear();
    }

    @EventHandler
    private void onChunkData(ChunkDataEvent event) {
        if (this.mc.world == null || this.mc.player == null) {
            return;
        }
        WorldChunk chunk = event.chunk();
        ChunkPos chunkPos = chunk.getPos();
        if (this.modifiedChunks.containsKey(chunkPos)) {
            return;
        }
        String reason = this.scanChunk(chunkPos, chunk);
        if (reason == null) {
            return;
        }
        this.modifiedChunks.put(chunkPos, reason);
        this.info("Modified chunk at " + chunkPos.x + ", " + chunkPos.z + " \u00a77(" + reason + ")", new Object[0]);
    }

    private String scanChunk(ChunkPos chunkPos, WorldChunk chunk) {
        int originX = chunkPos.getStartX();
        int originZ = chunkPos.getStartZ();
        int minY = Math.max(this.mc.world.getBottomY(), -64);
        int maxY = Math.min(minY + this.mc.world.getHeight() - 1, 0);
        BlockPos.Mutable cursor = new BlockPos.Mutable();
        for (int x = originX; x < originX + 16; ++x) {
            for (int z = originZ; z < originZ + 16; ++z) {
                for (int y = minY; y < maxY; ++y) {
                    cursor.set(x, y, z);
                    BlockState state = chunk.getBlockState((BlockPos)cursor);
                    if (PlayerChunkFinder.isRotatedDeepslate(state)) {
                        return "MODIFIED_DEEPSLATE";
                    }
                    if (!((Boolean)this.detectUnnatural.get()).booleanValue() || !PlayerChunkFinder.isUnnaturalUndergroundBlock(state.getBlock())) continue;
                    return "UNNATURAL";
                }
            }
        }
        return null;
    }

    private static boolean isRotatedDeepslate(BlockState state) {
        if (state.getBlock() != Blocks.DEEPSLATE) {
            return false;
        }
        if (!state.contains((Property)Properties.AXIS)) {
            return false;
        }
        return state.get((Property)Properties.AXIS) != Direction.Axis.Y;
    }

    private static boolean isUnnaturalUndergroundBlock(Block b) {
        return b == Blocks.COBBLESTONE || b == Blocks.COBBLED_DEEPSLATE || b == Blocks.OAK_PLANKS || b == Blocks.SPRUCE_PLANKS || b == Blocks.BIRCH_PLANKS || b == Blocks.JUNGLE_PLANKS || b == Blocks.ACACIA_PLANKS || b == Blocks.DARK_OAK_PLANKS || b == Blocks.MANGROVE_PLANKS || b == Blocks.CHERRY_PLANKS || b == Blocks.CRIMSON_PLANKS || b == Blocks.WARPED_PLANKS || b == Blocks.TORCH || b == Blocks.WALL_TORCH || b == Blocks.LADDER || b == Blocks.RAIL || b == Blocks.CRAFTING_TABLE || b == Blocks.FURNACE || b == Blocks.BLAST_FURNACE || b == Blocks.SMOKER || b == Blocks.CHEST || b == Blocks.TRAPPED_CHEST || b == Blocks.BARREL || b == Blocks.GLASS;
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (this.mc.player == null || this.modifiedChunks.isEmpty()) {
            return;
        }
        int radius = (Integer)this.renderRadius.get();
        int pcx = this.mc.player.getChunkPos().x;
        int pcz = this.mc.player.getChunkPos().z;
        double y = (Double)this.renderY.get();
        for (ChunkPos pos : this.modifiedChunks.keySet()) {
            if (Math.abs(pos.x - pcx) > radius || Math.abs(pos.z - pcz) > radius) continue;
            double x1 = pos.getStartX();
            double z1 = pos.getStartZ();
            if (((Boolean)this.filledEsp.get()).booleanValue()) {
                event.renderer.box(x1, y, z1, x1 + 16.0, y + 0.12, z1 + 16.0, (Color)this.fillColor.get(), (Color)this.lineColor.get(), ShapeMode.Both, 0);
                continue;
            }
            event.renderer.box(x1, y, z1, x1 + 16.0, y + 0.12, z1 + 16.0, new Color(0, 0, 0, 0), (Color)this.lineColor.get(), ShapeMode.Lines, 0);
        }
    }
}

