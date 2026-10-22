package com.aero.client.modules;

import com.aero.client.AeroCategory;
import java.util.ArrayList;
import java.util.List;
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
import net.minecraft.class_1297;
import net.minecraft.class_1456;
import net.minecraft.class_1542;
import net.minecraft.class_1657;
import net.minecraft.class_1923;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2586;
import net.minecraft.class_2595;
import net.minecraft.class_2614;
import net.minecraft.class_2818;

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
    public final Map<class_1923, FlaggedChunk> flagged;
    public final Set<class_1923> scanned;
    public final List<class_1923> queue;
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
        this.flagged = new ConcurrentHashMap<class_1923, FlaggedChunk>();
        this.scanned = ConcurrentHashMap.newKeySet();
        this.queue = new ArrayList<class_1923>();
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
        if (this.mc.field_1687 == null || this.mc.field_1724 == null) {
            return;
        }
        if (++this.tickCounter >= 200 || this.queue.isEmpty()) {
            this.tickCounter = 0;
            this.queue.clear();
            this.queueCursor = 0;
            class_1923 center = this.mc.field_1724.method_31476();
            int radius = (Integer)this.scanRadius.get();
            for (int cx = center.field_9181 - radius; cx <= center.field_9181 + radius; ++cx) {
                for (int cz = center.field_9180 - radius; cz <= center.field_9180 + radius; ++cz) {
                    class_1923 c = new class_1923(cx, cz);
                    if (this.scanned.contains(c)) continue;
                    this.queue.add(c);
                }
            }
            int keep = radius + 3;
            this.scanned.removeIf(p -> Math.abs(p.field_9181 - center.field_9181) > keep || Math.abs(p.field_9180 - center.field_9180) > keep);
            this.flagged.keySet().removeIf(p -> Math.abs(p.field_9181 - center.field_9181) > keep || Math.abs(p.field_9180 - center.field_9180) > keep);
        }
        int budget = (Integer)this.chunksPerTick.get();
        while (budget-- > 0 && this.queueCursor < this.queue.size()) {
            class_2818 chunk;
            class_1923 pos;
            if (!this.scanned.add(pos = this.queue.get(this.queueCursor++)) || (chunk = this.mc.field_1687.method_8497(pos.field_9181, pos.field_9180)) == null || chunk.method_12223()) continue;
            this.analyzeChunk(chunk, pos);
        }
    }

    public void analyzeChunk(class_2818 chunk, class_1923 pos) {
        int z;
        int x;
        if (this.mc.field_1687 == null) {
            return;
        }
        int originX = pos.method_8326();
        int originZ = pos.method_8328();
        int minY = this.mc.field_1687.method_31607();
        int maxY = minY + this.mc.field_1687.method_31605();
        class_2338.class_2339 cursor = new class_2338.class_2339();
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
                    class_2248 block;
                    cursor.method_10103(wx, y, wz);
                    try {
                        block = chunk.method_8320((class_2338)cursor).method_26204();
                    }
                    catch (Exception e) {
                        continue;
                    }
                    if (block == class_2246.field_10124 || block == class_2246.field_10543) {
                        if (++airRun > longestAir) {
                            longestAir = airRun;
                        }
                    } else {
                        if (longestAir >= 12 && y < 20) {
                            airPocket += longestAir / 4;
                        }
                        airRun = 0;
                    }
                    if (y < 0 && (block == class_2246.field_10445 || block == class_2246.field_29031 || block == class_2246.field_9989)) {
                        ++cobble;
                        cursor.method_10103(wx + 1, y, wz);
                        if (this.mc.field_1687.method_8320((class_2338)cursor).method_26204() == block) {
                            ++cobbleLines;
                        }
                        cursor.method_10103(wx, y, wz);
                    }
                    if (y < 0 && (block == class_2246.field_28892 || block == class_2246.field_28900 || block == class_2246.field_28896 || block == class_2246.field_28904)) {
                        ++deepslate;
                    }
                    if (y < 20 && block == class_2246.field_10540) {
                        ++obsidian;
                    }
                    if (y < 20 && ChunkFinder.isPiston(block)) {
                        ++pistons;
                    }
                    if (y < 0 && (block == class_2246.field_28048 || block == class_2246.field_28049)) {
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
                    if (block == class_2246.field_10442 || block == class_2246.field_29029) {
                        ++diamond;
                    }
                    if (block == class_2246.field_10212 || block == class_2246.field_29027) {
                        ++iron;
                    }
                    if (block == class_2246.field_10571 || block == class_2246.field_29026) {
                        ++gold;
                    }
                    if (block == class_2246.field_22109) {
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
                    cursor.method_10103(originX + x, y, originZ + z);
                    class_2248 b = chunk.method_8320((class_2338)cursor).method_26204();
                    if (b == class_2246.field_10124 || b == class_2246.field_10543) {
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
        for (class_1297 entity : this.mc.field_1687.method_18112()) {
            if (!new class_1923(entity.method_24515()).equals((Object)pos)) continue;
            if (entity instanceof class_1542 && entity.method_23318() < 0.0) {
                ++droppedItems;
            }
            if (entity instanceof class_1456) {
                polarBears += 3;
            }
            if (entity instanceof class_1657) continue;
            ++mobs;
        }
        if (mobs > 20) {
            mobCluster = mobs / 5;
        }
        int storage = 0;
        for (class_2586 be : chunk.method_12214().values()) {
            if (!(be instanceof class_2595) && !(be instanceof class_2614)) continue;
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
        this.info("Flagged " + pos.field_9181 + "," + pos.field_9180 + " | " + label, new Object[0]);
    }

    public boolean isHiddenOre(class_2338.class_2339 cursor, int x, int y, int z) {
        if (this.mc.field_1687 == null) {
            return false;
        }
        int enclosed = 0;
        for (class_2350 dir : class_2350.values()) {
            cursor.method_10103(x + dir.method_10148(), y + dir.method_10164(), z + dir.method_10165());
            class_2248 nb = this.mc.field_1687.method_8320((class_2338)cursor).method_26204();
            if (nb != class_2246.field_10445 && nb != class_2246.field_29031 && nb != class_2246.field_10340 && nb != class_2246.field_28888 && nb != class_2246.field_10540 && nb != class_2246.field_10515) continue;
            ++enclosed;
        }
        return enclosed >= 5;
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (this.mc.field_1687 == null || this.mc.field_1724 == null || this.flagged.isEmpty()) {
            return;
        }
        class_1923 center = this.mc.field_1724.method_31476();
        int radius = (Integer)this.scanRadius.get() + 1;
        for (FlaggedChunk fc : this.flagged.values()) {
            class_1923 cp = fc.pos();
            if (Math.abs(cp.field_9181 - center.field_9181) > radius || Math.abs(cp.field_9180 - center.field_9180) > radius) continue;
            double x1 = cp.method_8326();
            double z1 = cp.method_8328();
            event.renderer.box(x1, 63.0, z1, x1 + 16.0, 63.15, z1 + 16.0, (Color)this.fillColor.get(), (Color)this.lineColor.get(), ShapeMode.Both, 0);
        }
    }

    public static boolean isOre(class_2248 b) {
        return b == class_2246.field_10442 || b == class_2246.field_29029 || b == class_2246.field_10212 || b == class_2246.field_29027 || b == class_2246.field_10571 || b == class_2246.field_29026 || b == class_2246.field_10013 || b == class_2246.field_29220 || b == class_2246.field_10090 || b == class_2246.field_29028 || b == class_2246.field_10080 || b == class_2246.field_29030 || b == class_2246.field_10418 || b == class_2246.field_29219 || b == class_2246.field_27120 || b == class_2246.field_29221 || b == class_2246.field_22109;
    }

    public static boolean isStairBlock(class_2248 b) {
        return b == class_2246.field_10440 || b == class_2246.field_10596 || b == class_2246.field_28901 || b == class_2246.field_28893 || b == class_2246.field_10563 || b == class_2246.field_10569 || b == class_2246.field_10616 || b == class_2246.field_28889;
    }

    public static boolean isPiston(class_2248 b) {
        return b == class_2246.field_10560 || b == class_2246.field_10615 || b == class_2246.field_10008 || b == class_2246.field_10379;
    }

    public record FlaggedChunk(class_1923 pos, String label) {
    }
}

