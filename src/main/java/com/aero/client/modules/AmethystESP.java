package com.aero.client.modules;

import com.aero.client.AeroCategory;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.Text;
import net.minecraft.block.BlockState;
import net.minecraft.world.chunk.WorldChunk;
public class AmethystESP
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgRender;
    private final Setting<Integer> scanRadius;
    private final Setting<Integer> minClusters;
    private final Setting<Boolean> chatAlert;
    private final Setting<Boolean> showPillar;
    private final Setting<Boolean> showBoxes;
    private final Setting<Boolean> showTracers;
    private final Setting<SettingColor> pillarFill;
    private final Setting<SettingColor> pillarLine;
    private final Setting<SettingColor> budColor;
    private final Setting<SettingColor> tracerColor;
    private final Map<ChunkPos, List<BlockPos>> foundClusters;
    private final Set<ChunkPos> scanned;
    private final ArrayDeque<ChunkPos> scanQueue;
    private int lastCX;
    private int lastCZ;
    private int rescanTimer;
    private static final int RESCAN_INTERVAL = 100;

    public AmethystESP() {
        super(AeroCategory.CATEGORY, "amethyst-esp", "Scans loaded chunks for amethyst geodes and renders pillar highlights, bud boxes, and optional tracers.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgRender = this.settings.createGroup("Render");
        this.scanRadius = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("scan-radius")).description("Chunk radius to scan around you for amethyst geodes.")).defaultValue(4)).sliderMin(1).sliderMax(16).build());
        this.minClusters = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("min-buds")).description("Minimum number of amethyst buds/clusters in a chunk before it gets highlighted.")).defaultValue(14)).sliderMin(1).sliderMax(50).build());
        this.chatAlert = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("chat-alert")).description("Send a chat message when a new geode chunk is discovered.")).defaultValue(true)).build());
        this.showPillar = this.sgRender.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("show-pillar")).description("Render a full-height chunk column highlight over geode chunks.")).defaultValue(true)).build());
        this.showBoxes = this.sgRender.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("show-boxes")).description("Draw a box around each individual amethyst bud and cluster.")).defaultValue(true)).build());
        this.showTracers = this.sgRender.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("show-tracers")).description("Draw a line from your camera to each geode chunk.")).defaultValue(false)).build());
        this.pillarFill = this.sgRender.add((Setting)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("pillar-fill")).description("Fill color for the chunk pillar highlight.")).defaultValue(new SettingColor(155, 89, 182, 20)).build());
        this.pillarLine = this.sgRender.add((Setting)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("pillar-outline")).description("Outline color for the chunk pillar highlight.")).defaultValue(new SettingColor(155, 89, 182, 180)).build());
        this.budColor = this.sgRender.add((Setting)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("bud-color")).description("Color used for individual bud and cluster boxes.")).defaultValue(new SettingColor(200, 130, 255, 120)).build());
        this.tracerColor = this.sgRender.add((Setting)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("tracer-color")).description("Color of the tracer lines drawn to geode chunks.")).defaultValue(new SettingColor(155, 89, 182, 200)).build());
        this.foundClusters = new HashMap<ChunkPos, List<BlockPos>>();
        this.scanned = new HashSet<ChunkPos>();
        this.scanQueue = new ArrayDeque();
        this.lastCX = Integer.MIN_VALUE;
        this.lastCZ = Integer.MIN_VALUE;
        this.rescanTimer = 0;
    }

    public void onActivate() {
        this.foundClusters.clear();
        this.scanned.clear();
        this.scanQueue.clear();
        this.lastCX = Integer.MIN_VALUE;
        this.lastCZ = Integer.MIN_VALUE;
    }

    public void onDeactivate() {
        this.foundClusters.clear();
        this.scanned.clear();
        this.scanQueue.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        boolean forceRescan;
        if (this.mc.world == null || this.mc.player == null) {
            return;
        }
        int pcx = this.mc.player.getChunkPos().x;
        int pcz = this.mc.player.getChunkPos().z;
        boolean movedChunk = pcx != this.lastCX || pcz != this.lastCZ;
        boolean bl = forceRescan = ++this.rescanTimer >= 100;
        if (movedChunk || forceRescan) {
            this.lastCX = pcx;
            this.lastCZ = pcz;
            if (forceRescan) {
                this.rescanTimer = 0;
                this.scanned.clear();
            }
            this.rebuildQueue(pcx, pcz);
        }
        int done = 0;
        while (!this.scanQueue.isEmpty() && done < 2) {
            ChunkPos cp = this.scanQueue.poll();
            if (this.scanned.contains(cp)) continue;
            this.scanChunk(cp);
            this.scanned.add(cp);
            ++done;
        }
    }

    private void rebuildQueue(int pcx, int pcz) {
        int r = (Integer)this.scanRadius.get();
        HashSet<ChunkPos> inRange = new HashSet<ChunkPos>();
        for (int dx = -r; dx <= r; ++dx) {
            for (int dz = -r; dz <= r; ++dz) {
                inRange.add(new ChunkPos(pcx + dx, pcz + dz));
            }
        }
        this.scanned.removeIf(cp -> !inRange.contains(cp));
        this.foundClusters.keySet().removeIf(cp -> !inRange.contains(cp));
        ArrayList<ChunkPos> toScan = new ArrayList<ChunkPos>();
        for (ChunkPos cp2 : inRange) {
            if (this.scanned.contains(cp2)) continue;
            toScan.add(cp2);
        }
        toScan.sort(Comparator.comparingInt(c -> {
            int dx = c.x - pcx;
            int dz = c.z - pcz;
            return dx * dx + dz * dz;
        }));
        this.scanQueue.clear();
        this.scanQueue.addAll(toScan);
    }

    private void scanChunk(ChunkPos cp) {
        if (this.mc.world == null) {
            return;
        }
        WorldChunk chunk = this.mc.world.getChunk(cp.x, cp.z);
        if (chunk == null) {
            return;
        }
        ArrayList<BlockPos> buds = new ArrayList<BlockPos>();
        int bx = cp.x * 16;
        int bz = cp.z * 16;
        for (int x = bx; x < bx + 16; ++x) {
            for (int z = bz; z < bz + 16; ++z) {
                for (int y = this.mc.world.getBottomY(); y < this.mc.world.getBottomY() + this.mc.world.getHeight(); ++y) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = this.mc.world.getBlockState(pos);
                    if (!state.isOf(Blocks.AMETHYST_CLUSTER) && !state.isOf(Blocks.LARGE_AMETHYST_BUD)) continue;
                    buds.add(pos);
                }
            }
        }
        if (buds.size() >= (Integer)this.minClusters.get()) {
            this.foundClusters.put(cp, buds);
            if (((Boolean)this.chatAlert.get()).booleanValue() && this.mc.player != null) {
                this.mc.player.sendMessage((Text)Text.literal((String)("\u00a7d[ClusterBypass] \u00a7fGeode at X" + cp.x * 16 + " Z" + cp.z * 16 + " \u00a77(" + buds.size() + " buds)")), false);
            }
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (this.mc.player == null || this.mc.world == null) {
            return;
        }
        Vec3d cam = this.mc.player.getCameraPosVec(1.0f);
        for (Map.Entry<ChunkPos, List<BlockPos>> e : this.foundClusters.entrySet()) {
            ChunkPos cp = e.getKey();
            List<BlockPos> buds = e.getValue();
            if (((Boolean)this.showPillar.get()).booleanValue()) {
                event.renderer.box((double)(cp.x * 16), (double)this.mc.world.getBottomY(), (double)(cp.z * 16), (double)(cp.x * 16 + 16), (double)(this.mc.world.getBottomY() + this.mc.world.getHeight()), (double)(cp.z * 16 + 16), (Color)this.pillarFill.get(), (Color)this.pillarLine.get(), ShapeMode.Both, 0);
            }
            if (((Boolean)this.showBoxes.get()).booleanValue()) {
                for (BlockPos p : buds) {
                    event.renderer.box((double)p.getX(), (double)p.getY(), (double)p.getZ(), (double)(p.getX() + 1), (double)(p.getY() + 1), (double)(p.getZ() + 1), (Color)new SettingColor(((SettingColor)this.budColor.get()).r, ((SettingColor)this.budColor.get()).g, ((SettingColor)this.budColor.get()).b, 40), (Color)this.budColor.get(), ShapeMode.Both, 0);
                }
            }
            if (!((Boolean)this.showTracers.get()).booleanValue() || buds.isEmpty()) continue;
            BlockPos p = buds.get(0);
            event.renderer.line(cam.x, cam.y, cam.z, (double)p.getX() + 0.5, (double)p.getY() + 0.5, (double)p.getZ() + 0.5, (Color)this.tracerColor.get());
        }
    }
}

