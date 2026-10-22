package com.aero.client.modules;

import com.aero.client.AeroCategory;
import java.util.ArrayList;
import java.util.List;
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
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.PolarBearEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.world.chunk.WorldChunk;
public class ChunkFinder
extends Module {
    private final SettingGroup sg;
    public final Setting<Integer> scanRadius;
    public final Setting<Integer> minScore;
    public final Setting<Integer> chunksPerTick;
    public final Setting<SettingColor> fillColor;
    public final Setting<SettingColor> lineColor;
    public final Setting<Boolean> detCobble;
    public final Setting<Boolean> detDeepslate;
    public final Setting<Boolean> detObsidian;
    public final Setting<Boolean> detVeinInt;
    public final Setting<Boolean> detVertVein;
    public final Setting<Boolean> detNeedle;
    public final Setting<Boolean> detHiddenOre;
    public final Setting<Boolean> detPiston;
    public final Setting<Boolean> detDripstone;
    public final Setting<Boolean> detMobCluster;
    public final Setting<Boolean> detDropped;
    public final Setting<Boolean> detAirPocket;
    public final Setting<Boolean> detStair;
    public final Setting<Boolean> detPolarBear;
    public final Map<ChunkPos, FlaggedChunk> flagged;
    public final Set<ChunkPos> scanned;
    public final List<ChunkPos> queue;
    public int queueCursor;
    public int tickCounter;

    public ChunkFinder() {
        super(AeroCategory.CATEGORY, "chunk-finder", "Multi-detector base hunter: scans chunks for 14 signs of player activity.");
        this.sg = this.settings.getDefaultGroup();
        this.scanRadius = this.sg.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("scan-radius")).description("Scan radius in chunks.")).defaultValue(5)).min(1).sliderMax(12).build());
        this.minScore = this.sg.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("min-score")).description("Minimum score to flag a chunk.")).defaultValue(5)).min(1).sliderMax(30).build());
        this.chunksPerTick = this.sg.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("chunks-per-tick")).description("Chunks scanned per tick.")).defaultValue(2)).min(1).sliderMax(8).build());
        this.fillColor = this.sg.add((Setting)((ColorSetting.Builder)new ColorSetting.Builder().name("fill-color")).defaultValue(new SettingColor(0, 255, 80, 45)).build());
        this.lineColor = this.sg.add((Setting)((ColorSetting.Builder)new ColorSetting.Builder().name("line-color")).defaultValue(new SettingColor(0, 255, 80, 200)).build());
        this.detCobble = this.sg.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("cobble-lines")).defaultValue(true)).build());
        this.detDeepslate = this.sg.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("deepslate-band")).defaultValue(true)).build());
        this.detObsidian = this.sg.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("obsidian-backbone")).defaultValue(true)).build());
        this.detVeinInt = this.sg.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("vein-integrity")).defaultValue(true)).build());
        this.detVertVein = this.sg.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("vertical-vein")).defaultValue(true)).build());
        this.detNeedle = this.sg.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("needle-hole")).defaultValue(true)).build());
        this.detHiddenOre = this.sg.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("hidden-ore")).defaultValue(true)).build());
        this.detPiston = this.sg.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("piston-array")).defaultValue(true)).build());
        this.detDripstone = this.sg.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("dripstone")).defaultValue(true)).build());
        this.detMobCluster = this.sg.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("mob-cluster")).defaultValue(true)).build());
        this.detDropped = this.sg.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("dropped-items")).defaultValue(true)).build());
        this.detAirPocket = this.sg.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("air-pocket")).defaultValue(true)).build());
        this.detStair = this.sg.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("synthetic-stair")).defaultValue(true)).build());
        this.detPolarBear = this.sg.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("polar-bear")).defaultValue(true)).build());
        this.flagged = new ConcurrentHashMap<ChunkPos, FlaggedChunk>();
        this.scanned = ConcurrentHashMap.newKeySet();
        this.queue = new ArrayList<ChunkPos>();
        this.queueCursor = 0;
        this.tickCounter = 0;
    }

    public void onActivate() {
        this.clear();
    }

    public void onDeactivate() {
        this.clear();
    }

    public void clear() {
        this.flagged.clear();
        this.scanned.clear();
        this.queue.clear();
        this.queueCursor = 0;
        this.tickCounter = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (this.mc.world == null || this.mc.player == null) {
            return;
        }
        if (++this.tickCounter >= 200 || this.queue.isEmpty()) {
            this.tickCounter = 0;
            this.queue.clear();
            this.queueCursor = 0;
            ChunkPos center = this.mc.player.getChunkPos();
            int radius = (Integer)this.scanRadius.get();
            for (int cx = center.x - radius; cx <= center.x + radius; ++cx) {
                for (int cz = center.z - radius; cz <= center.z + radius; ++cz) {
                    ChunkPos c = new ChunkPos(cx, cz);
                    if (this.scanned.contains(c)) continue;
                    this.queue.add(c);
                }
            }
            int keep = radius + 3;
            this.scanned.removeIf(p -> Math.abs(p.x - center.x) > keep || Math.abs(p.z - center.z) > keep);
            this.flagged.keySet().removeIf(p -> Math.abs(p.x - center.x) > keep || Math.abs(p.z - center.z) > keep);
        }
        int budget = (Integer)this.chunksPerTick.get();
        while (budget-- > 0 && this.queueCursor < this.queue.size()) {
            WorldChunk chunk;
            ChunkPos pos;
            if (!this.scanned.add(pos = this.queue.get(this.queueCursor++)) || (chunk = this.mc.world.getChunk(pos.x, pos.z)) == null || chunk.isEmpty()) continue;
            this.analyzeChunk(chunk, pos);
        }
    }

    public void analyzeChunk(WorldChunk chunk, ChunkPos pos) {
        int z;
        int x;
        if (this.mc.world == null) {
            return;
        }
        int originX = pos.getStartX();
        int originZ = pos.getStartZ();
        int minY = this.mc.world.getBottomY();
        int maxY = minY + this.mc.world.getHeight();
        BlockPos.Mutable cursor = new BlockPos.Mutable();
        int cobble = 0;
        int cobbleLines = 0;
        int deepslate = 0;
        int obsidian = 0;
        int pistons = 0;
        int dripstone = 0;
        int airPocket = 0;
        int stairs = 0;
        int diamond = 0;
        int iron = 0;
        int gold = 0;
        int debris = 0;
        int[] oreByLayer = new int[Math.max(1, maxY - minY + 1)];
        int needles = 0;
        int hiddenOre = 0;
        int[][] oreColumns = new int[16][16];
        for (x = 0; x < 16; ++x) {
            for (z = 0; z < 16; ++z) {
                int wx = originX + x;
                int wz = originZ + z;
                int airRun = 0;
                int longestAir = 0;
                for (int y = minY; y <= maxY; ++y) {
                    Block block;
                    cursor.set(wx, y, wz);
                    try {
                        block = chunk.getBlockState((BlockPos)cursor).getBlock();
                    }
                    catch (Exception e) {
                        continue;
                    }
                    if (block == Blocks.AIR || block == Blocks.CAVE_AIR) {
                        if (++airRun > longestAir) {
                            longestAir = airRun;
                        }
                    } else {
                        if (longestAir >= 12 && y < 20) {
                            airPocket += longestAir / 4;
                        }
                        airRun = 0;
                    }
                    if (y < 0 && (block == Blocks.COBBLESTONE || block == Blocks.COBBLED_DEEPSLATE || block == Blocks.MOSSY_COBBLESTONE)) {
                        ++cobble;
                        cursor.set(wx + 1, y, wz);
                        if (this.mc.world.getBlockState((BlockPos)cursor).getBlock() == block) {
                            ++cobbleLines;
                        }
                        cursor.set(wx, y, wz);
                    }
                    if (y < 0 && (block == Blocks.POLISHED_DEEPSLATE || block == Blocks.DEEPSLATE_BRICKS || block == Blocks.DEEPSLATE_TILES || block == Blocks.CHISELED_DEEPSLATE)) {
                        ++deepslate;
                    }
                    if (y < 20 && block == Blocks.OBSIDIAN) {
                        ++obsidian;
                    }
                    if (y < 20 && ChunkFinder.isPiston(block)) {
                        ++pistons;
                    }
                    if (y < 0 && (block == Blocks.POINTED_DRIPSTONE || block == Blocks.DRIPSTONE_BLOCK)) {
                        ++dripstone;
                    }
                    if (y < 0 && ChunkFinder.isStairBlock(block)) {
                        ++stairs;
                    }
                    if (!ChunkFinder.isOre(block)) {
                        oreColumns[x][z] = 0;
                        continue;
                    }
                    int layer = y - minY;
                    if (layer < oreByLayer.length) {
                        int n = layer;
                        oreByLayer[n] = oreByLayer[n] + 1;
                    }
                    int[] nArray = oreColumns[x];
                    int n = z;
                    nArray[n] = nArray[n] + 1;
                    if (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE) {
                        ++diamond;
                    }
                    if (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE) {
                        ++iron;
                    }
                    if (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE) {
                        ++gold;
                    }
                    if (block == Blocks.ANCIENT_DEBRIS) {
                        ++debris;
                    }
                    if (!this.isHiddenOre(cursor, wx, y, wz)) continue;
                    hiddenOre += 2;
                }
            }
        }
        for (x = 0; x < 16; ++x) {
            for (z = 0; z < 16; ++z) {
                int airRun = 0;
                for (int y = minY; y < 0; ++y) {
                    cursor.set(originX + x, y, originZ + z);
                    Block b = chunk.getBlockState((BlockPos)cursor).getBlock();
                    if (b == Blocks.AIR || b == Blocks.CAVE_AIR) {
                        ++airRun;
                        continue;
                    }
                    airRun = 0;
                }
                if (airRun < 10) continue;
                ++needles;
            }
        }
        int vertVeins = 0;
        for (int x2 = 0; x2 < 16; ++x2) {
            for (int z2 = 0; z2 < 16; ++z2) {
                if (oreColumns[x2][z2] < 4) continue;
                ++vertVeins;
            }
        }
        int mobCluster = 0;
        int droppedItems = 0;
        int polarBears = 0;
        int mobs = 0;
        for (Entity entity : this.mc.world.getEntities()) {
            if (!new ChunkPos(entity.getBlockPos()).equals((Object)pos)) continue;
            if (entity instanceof ItemEntity && entity.getY() < 0.0) {
                ++droppedItems;
            }
            if (entity instanceof PolarBearEntity) {
                polarBears += 3;
            }
            if (entity instanceof PlayerEntity) continue;
            ++mobs;
        }
        if (mobs > 20) {
            mobCluster = mobs / 5;
        }
        int storage = 0;
        for (BlockEntity be : chunk.getBlockEntities().values()) {
            if (!(be instanceof ChestBlockEntity) && !(be instanceof HopperBlockEntity)) continue;
            ++storage;
        }
        int score = 0;
        ArrayList<String> tags = new ArrayList<String>();
        if (((Boolean)this.detCobble.get()).booleanValue() && cobbleLines >= 3) {
            score += Math.min(cobbleLines, 6);
            tags.add("CobbleLine(" + cobbleLines + ")");
        }
        if (((Boolean)this.detDeepslate.get()).booleanValue() && deepslate >= 4) {
            score += Math.min(deepslate / 2, 5);
            tags.add("Deepslate(" + deepslate + ")");
        }
        if (((Boolean)this.detObsidian.get()).booleanValue() && obsidian >= 3) {
            score += Math.min(obsidian, 6);
            tags.add("Obsidian(" + obsidian + ")");
        }
        if (((Boolean)this.detVeinInt.get()).booleanValue() && diamond + debris >= 1 && storage >= 1) {
            score += 4;
            tags.add("VeinInt");
        }
        if (((Boolean)this.detVertVein.get()).booleanValue() && vertVeins >= 2) {
            score += vertVeins;
            tags.add("VertVein(" + vertVeins + ")");
        }
        if (((Boolean)this.detNeedle.get()).booleanValue() && needles >= 2) {
            score += needles * 2;
            tags.add("Needle(" + needles + ")");
        }
        if (((Boolean)this.detHiddenOre.get()).booleanValue() && hiddenOre >= 2) {
            score += hiddenOre;
            tags.add("HiddenOre(" + hiddenOre + ")");
        }
        if (((Boolean)this.detPiston.get()).booleanValue() && pistons >= 3) {
            score += Math.min(pistons, 8);
            tags.add("PistonArray(" + pistons + ")");
        }
        if (((Boolean)this.detDripstone.get()).booleanValue() && dripstone >= 6) {
            score += dripstone / 3;
            tags.add("Dripstone(" + dripstone + ")");
        }
        if (((Boolean)this.detMobCluster.get()).booleanValue() && mobCluster >= 2) {
            score += mobCluster;
            tags.add("MobCluster(" + mobCluster * 5 + ")");
        }
        if (((Boolean)this.detDropped.get()).booleanValue() && droppedItems >= 2) {
            score += droppedItems * 2;
            tags.add("Dropped(" + droppedItems + ")");
        }
        if (((Boolean)this.detAirPocket.get()).booleanValue() && airPocket >= 5) {
            score += airPocket / 3;
            tags.add("AirPocket(" + airPocket + ")");
        }
        if (((Boolean)this.detStair.get()).booleanValue() && stairs >= 5) {
            score += stairs / 3;
            tags.add("Stairs(" + stairs + ")");
        }
        if (((Boolean)this.detPolarBear.get()).booleanValue() && polarBears > 0) {
            score += polarBears;
            tags.add("PolarBear");
        }
        if (storage >= 3) {
            score += storage;
            tags.add("Storage(" + storage + ")");
        }
        if (score < (Integer)this.minScore.get()) {
            return;
        }
        String label = "Base? S:" + score + " [" + String.join((CharSequence)", ", tags) + "]";
        this.flagged.put(pos, new FlaggedChunk(pos, label));
        this.info("Flagged " + pos.x + "," + pos.z + " | " + label, new Object[0]);
    }

    public boolean isHiddenOre(BlockPos.Mutable cursor, int x, int y, int z) {
        if (this.mc.world == null) {
            return false;
        }
        int enclosed = 0;
        for (Direction dir : Direction.values()) {
            cursor.set(x + dir.getOffsetX(), y + dir.getOffsetY(), z + dir.getOffsetZ());
            Block nb = this.mc.world.getBlockState((BlockPos)cursor).getBlock();
            if (nb != Blocks.COBBLESTONE && nb != Blocks.COBBLED_DEEPSLATE && nb != Blocks.STONE && nb != Blocks.DEEPSLATE && nb != Blocks.OBSIDIAN && nb != Blocks.NETHERRACK) continue;
            ++enclosed;
        }
        return enclosed >= 5;
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (this.mc.world == null || this.mc.player == null || this.flagged.isEmpty()) {
            return;
        }
        ChunkPos center = this.mc.player.getChunkPos();
        int radius = (Integer)this.scanRadius.get() + 1;
        for (FlaggedChunk fc : this.flagged.values()) {
            ChunkPos cp = fc.pos();
            if (Math.abs(cp.x - center.x) > radius || Math.abs(cp.z - center.z) > radius) continue;
            double x1 = cp.getStartX();
            double z1 = cp.getStartZ();
            event.renderer.box(x1, 63.0, z1, x1 + 16.0, 63.15, z1 + 16.0, (Color)this.fillColor.get(), (Color)this.lineColor.get(), ShapeMode.Both, 0);
        }
    }

    public static boolean isOre(Block b) {
        return b == Blocks.DIAMOND_ORE || b == Blocks.DEEPSLATE_DIAMOND_ORE || b == Blocks.IRON_ORE || b == Blocks.DEEPSLATE_IRON_ORE || b == Blocks.GOLD_ORE || b == Blocks.DEEPSLATE_GOLD_ORE || b == Blocks.EMERALD_ORE || b == Blocks.DEEPSLATE_EMERALD_ORE || b == Blocks.LAPIS_ORE || b == Blocks.DEEPSLATE_LAPIS_ORE || b == Blocks.REDSTONE_ORE || b == Blocks.DEEPSLATE_REDSTONE_ORE || b == Blocks.COAL_ORE || b == Blocks.DEEPSLATE_COAL_ORE || b == Blocks.COPPER_ORE || b == Blocks.DEEPSLATE_COPPER_ORE || b == Blocks.ANCIENT_DEBRIS;
    }

    public static boolean isStairBlock(Block b) {
        return b == Blocks.STONE_STAIRS || b == Blocks.COBBLESTONE_STAIRS || b == Blocks.DEEPSLATE_BRICK_STAIRS || b == Blocks.POLISHED_DEEPSLATE_STAIRS || b == Blocks.OAK_STAIRS || b == Blocks.SPRUCE_STAIRS || b == Blocks.DARK_OAK_STAIRS || b == Blocks.COBBLED_DEEPSLATE_STAIRS;
    }

    public static boolean isPiston(Block b) {
        return b == Blocks.PISTON || b == Blocks.STICKY_PISTON || b == Blocks.MOVING_PISTON || b == Blocks.PISTON_HEAD;
    }

    public record FlaggedChunk(ChunkPos pos, String label) {
    }
}

