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
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2680;
import net.minecraft.class_2818;

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
    private final Map<class_1923, List<class_2338>> foundClusters;
    private final Set<class_1923> scanned;
    private final ArrayDeque<class_1923> scanQueue;
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
        this.foundClusters = new HashMap<class_1923, List<class_2338>>();
        this.scanned = new HashSet<class_1923>();
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
        if (this.mc.field_1687 == null || this.mc.field_1724 == null) {
            return;
        }
        int pcx = this.mc.field_1724.method_31476().field_9181;
        int pcz = this.mc.field_1724.method_31476().field_9180;
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
            class_1923 cp = this.scanQueue.poll();
            if (this.scanned.contains(cp)) continue;
            this.scanChunk(cp);
            this.scanned.add(cp);
            ++done;
        }
    }

    private void rebuildQueue(int pcx, int pcz) {
        int r = (Integer)this.scanRadius.get();
        HashSet<class_1923> inRange = new HashSet<class_1923>();
        for (int dx = -r; dx <= r; ++dx) {
            for (int dz = -r; dz <= r; ++dz) {
                inRange.add(new class_1923(pcx + dx, pcz + dz));
            }
        }
        this.scanned.removeIf(cp -> !inRange.contains(cp));
        this.foundClusters.keySet().removeIf(cp -> !inRange.contains(cp));
        ArrayList<class_1923> toScan = new ArrayList<class_1923>();
        for (class_1923 cp2 : inRange) {
            if (this.scanned.contains(cp2)) continue;
            toScan.add(cp2);
        }
        toScan.sort(Comparator.comparingInt(c -> {
            int dx = c.field_9181 - pcx;
            int dz = c.field_9180 - pcz;
            return dx * dx + dz * dz;
        }));
        this.scanQueue.clear();
        this.scanQueue.addAll(toScan);
    }

    private void scanChunk(class_1923 cp) {
        if (this.mc.field_1687 == null) {
            return;
        }
        class_2818 chunk = this.mc.field_1687.method_8497(cp.field_9181, cp.field_9180);
        if (chunk == null) {
            return;
        }
        ArrayList<class_2338> buds = new ArrayList<class_2338>();
        int bx = cp.field_9181 * 16;
        int bz = cp.field_9180 * 16;
        for (int x = bx; x < bx + 16; ++x) {
            for (int z = bz; z < bz + 16; ++z) {
                for (int y = this.mc.field_1687.method_31607(); y < this.mc.field_1687.method_31607() + this.mc.field_1687.method_31605(); ++y) {
                    class_2338 pos = new class_2338(x, y, z);
                    class_2680 state = this.mc.field_1687.method_8320(pos);
                    if (!state.method_27852(class_2246.field_27161) && !state.method_27852(class_2246.field_27162)) continue;
                    buds.add(pos);
                }
            }
        }
        if (buds.size() >= (Integer)this.minClusters.get()) {
            this.foundClusters.put(cp, buds);
            if (((Boolean)this.chatAlert.get()).booleanValue() && this.mc.field_1724 != null) {
                this.mc.field_1724.method_7353((class_2561)class_2561.method_43470((String)("\u00a7d[ClusterBypass] \u00a7fGeode at X" + cp.field_9181 * 16 + " Z" + cp.field_9180 * 16 + " \u00a77(" + buds.size() + " buds)")), false);
            }
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
            return;
        }
        class_243 cam = this.mc.field_1724.method_5836(1.0f);
        for (Map.Entry<class_1923, List<class_2338>> e : this.foundClusters.entrySet()) {
            class_1923 cp = e.getKey();
            List<class_2338> buds = e.getValue();
            if (((Boolean)this.showPillar.get()).booleanValue()) {
                event.renderer.box((double)(cp.field_9181 * 16), (double)this.mc.field_1687.method_31607(), (double)(cp.field_9180 * 16), (double)(cp.field_9181 * 16 + 16), (double)(this.mc.field_1687.method_31607() + this.mc.field_1687.method_31605()), (double)(cp.field_9180 * 16 + 16), (Color)this.pillarFill.get(), (Color)this.pillarLine.get(), ShapeMode.Both, 0);
            }
            if (((Boolean)this.showBoxes.get()).booleanValue()) {
                for (class_2338 p : buds) {
                    event.renderer.box((double)p.method_10263(), (double)p.method_10264(), (double)p.method_10260(), (double)(p.method_10263() + 1), (double)(p.method_10264() + 1), (double)(p.method_10260() + 1), (Color)new SettingColor(((SettingColor)this.budColor.get()).r, ((SettingColor)this.budColor.get()).g, ((SettingColor)this.budColor.get()).b, 40), (Color)this.budColor.get(), ShapeMode.Both, 0);
                }
            }
            if (!((Boolean)this.showTracers.get()).booleanValue() || buds.isEmpty()) continue;
            class_2338 p = buds.get(0);
            event.renderer.line(cam.field_1352, cam.field_1351, cam.field_1350, (double)p.method_10263() + 0.5, (double)p.method_10264() + 0.5, (double)p.method_10260() + 0.5, (Color)this.tracerColor.get());
        }
    }
}

