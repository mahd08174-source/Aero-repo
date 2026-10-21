package com.aero.client.modules;

import com.aero.client.AeroCategory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1923;
import net.minecraft.class_2246;
import net.minecraft.class_2462;
import net.minecraft.class_2561;
import net.minecraft.class_2586;
import net.minecraft.class_2680;
import net.minecraft.class_2769;
import net.minecraft.class_2818;
import net.minecraft.class_2826;
import net.minecraft.class_4482;

public class TuffChunkFinder
extends Module {
    private static final double RENDER_Y = 63.0;
    private static final double CHUNK_THICKNESS = 0.01;
    private static final long FULL_SCAN_INTERVAL = 20000L;
    private static final long QUICK_SCAN_INTERVAL = 200L;
    private static final long COOLDOWN_MS = 50000L;
    private final ConcurrentHashMap<class_1923, String> foundChunks = new ConcurrentHashMap();
    private final AtomicBoolean scanning = new AtomicBoolean(false);
    private ExecutorService executor;
    private long lastFullScanTime = 0L;
    private long lastQuickScanTime = 0L;
    private long lastMarkTime = 0L;

    public TuffChunkFinder() {
        super(AeroCategory.CATEGORY, "tuff-chunk-finder", "Scans nearby chunks for powered repeaters, hives, cobbled deepslate and other base indicators.");
    }

    public void onActivate() {
        this.foundChunks.clear();
        this.lastFullScanTime = 0L;
        this.lastQuickScanTime = 0L;
        this.scanning.set(false);
    }

    public void onDeactivate() {
        this.foundChunks.clear();
        if (this.executor != null) {
            this.executor.shutdownNow();
        }
        this.scanning.set(false);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (this.mc.field_1687 == null || this.mc.field_1724 == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - this.lastQuickScanTime >= 200L && !this.scanning.get()) {
            this.lastQuickScanTime = now;
            this.quickRepeaterScan();
        }
        if (now - this.lastFullScanTime >= 20000L && this.scanning.compareAndSet(false, true)) {
            this.lastFullScanTime = now;
            this.fullScan();
        }
    }

    private void quickRepeaterScan() {
        if (this.mc.field_1687 == null || this.mc.field_1724 == null) {
            return;
        }
        ArrayList<class_1923> positions = new ArrayList<class_1923>();
        ArrayList<class_2818> chunks = new ArrayList<class_2818>();
        this.collectChunks(positions, chunks);
        this.getOrCreateExecutor("tuff-quick-scan").submit(() -> {
            for (int i = 0; i < positions.size(); ++i) {
                if (this.countPoweredRepeaters((class_2818)chunks.get(i)) < 3) continue;
                this.foundChunks.clear();
                this.foundChunks.put((class_1923)positions.get(i), "repeater");
                return;
            }
        });
    }

    private void fullScan() {
        if (this.mc.field_1687 == null || this.mc.field_1724 == null) {
            this.scanning.set(false);
            return;
        }
        ArrayList<class_1923> positions = new ArrayList<class_1923>();
        ArrayList<class_2818> chunks = new ArrayList<class_2818>();
        this.collectChunks(positions, chunks);
        this.getOrCreateExecutor("tuff-full-scan").submit(() -> {
            try {
                int i;
                for (i = 0; i < positions.size(); ++i) {
                    if (this.countPoweredRepeaters((class_2818)chunks.get(i)) < 3) continue;
                    this.lastMarkTime = System.currentTimeMillis();
                    this.foundChunks.clear();
                    this.foundChunks.put((class_1923)positions.get(i), "repeater");
                    this.notifyFound("repeater");
                    return;
                }
                for (i = 0; i < positions.size(); ++i) {
                    String type;
                    boolean hasRepeater;
                    class_2818 chunk = (class_2818)chunks.get(i);
                    boolean hasHive = this.checkHive(chunk);
                    boolean hasDeepslate = !hasHive && this.checkCobbledDeepslate(chunk);
                    boolean hasVines = !hasHive && this.checkVines(chunk);
                    boolean hasSeagrass = !hasHive && !hasVines && this.checkSeagrass(chunk);
                    boolean bl = hasRepeater = !hasHive && !hasVines && !hasSeagrass && !hasDeepslate && this.checkAnyRepeaters(chunk);
                    if (!hasHive && !hasDeepslate && !hasVines && !hasSeagrass && !hasRepeater) continue;
                    String string = type = hasRepeater ? "repeater" : "normal";
                    if ("repeater".equals(type)) {
                        this.lastMarkTime = System.currentTimeMillis();
                        this.foundChunks.clear();
                        this.foundChunks.put((class_1923)positions.get(i), type);
                        this.notifyFound("repeater");
                    } else if (System.currentTimeMillis() - this.lastMarkTime >= 50000L) {
                        this.lastMarkTime = System.currentTimeMillis();
                        this.foundChunks.clear();
                        this.foundChunks.put((class_1923)positions.get(i), type);
                        this.notifyFound("normal");
                    }
                    return;
                }
            }
            finally {
                this.scanning.set(false);
            }
        });
    }

    private void collectChunks(List<class_1923> positions, List<class_2818> chunks) {
        class_1923 pc = this.mc.field_1724.method_31476();
        int dist = Math.min((Integer)this.mc.field_1690.method_42503().method_41753(), 8);
        for (int dx = -dist; dx <= dist; ++dx) {
            for (int dz = -dist; dz <= dist; ++dz) {
                class_1923 cp = new class_1923(pc.field_9181 + dx, pc.field_9180 + dz);
                class_2818 chunk = this.mc.field_1687.method_2935().method_21730(cp.field_9181, cp.field_9180);
                if (chunk == null || chunk.method_12223()) continue;
                positions.add(cp);
                chunks.add(chunk);
            }
        }
    }

    private int countPoweredRepeaters(class_2818 chunk) {
        int count = 0;
        for (class_2826 section : chunk.method_12006()) {
            if (section == null || section.method_38292() || !section.method_19523(bs -> bs.method_27852(class_2246.field_10450))) continue;
            for (int lx = 0; lx < 16; ++lx) {
                for (int ly = 0; ly < 16; ++ly) {
                    for (int lz = 0; lz < 16; ++lz) {
                        class_2680 bs2 = section.method_12254(lx, ly, lz);
                        if (!bs2.method_27852(class_2246.field_10450) || !((Boolean)bs2.method_11654((class_2769)class_2462.field_10911)).booleanValue() || ++count < 3) continue;
                        return count;
                    }
                }
            }
        }
        return count;
    }

    private boolean checkHive(class_2818 chunk) {
        for (class_2586 be : chunk.method_12214().values()) {
            class_4482 hive;
            if (!(be instanceof class_4482) || (hive = (class_4482)be).method_23903() <= 0) continue;
            return true;
        }
        return false;
    }

    private boolean checkCobbledDeepslate(class_2818 chunk) {
        int sectionY;
        int count = 0;
        int bottomY = chunk.method_31607();
        class_2826[] sections = chunk.method_12006();
        for (int si = 0; si < sections.length && (sectionY = bottomY + si * 16) <= 20; ++si) {
            class_2826 s;
            if (sectionY + 16 < 0 || (s = sections[si]) == null || s.method_38292() || !s.method_19523(bs -> bs.method_27852(class_2246.field_29031))) continue;
            for (int lx = 0; lx < 16; ++lx) {
                for (int ly = 0; ly < 16; ++ly) {
                    for (int lz = 0; lz < 16; ++lz) {
                        int wy = sectionY + ly;
                        if (wy < 0 || wy > 20 || !s.method_12254(lx, ly, lz).method_27852(class_2246.field_29031) || ++count < 50) continue;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean checkVines(class_2818 chunk) {
        int count = 0;
        for (class_2826 s : chunk.method_12006()) {
            if (s == null || s.method_38292() || !s.method_19523(bs -> bs.method_27852(class_2246.field_10597))) continue;
            for (int lx = 0; lx < 16; ++lx) {
                for (int ly = 0; ly < 16; ++ly) {
                    for (int lz = 0; lz < 16; ++lz) {
                        if (!s.method_12254(lx, ly, lz).method_27852(class_2246.field_10597) || ++count < 150) continue;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean checkSeagrass(class_2818 chunk) {
        int sectionY;
        int count = 0;
        int bottomY = chunk.method_31607();
        class_2826[] sections = chunk.method_12006();
        for (int si = 0; si < sections.length && (sectionY = bottomY + si * 16) <= 70; ++si) {
            class_2826 s = sections[si];
            if (s == null || s.method_38292() || !s.method_19523(bs -> bs.method_27852(class_2246.field_10376) || bs.method_27852(class_2246.field_10238))) continue;
            for (int lx = 0; lx < 16; ++lx) {
                for (int ly = 0; ly < 16; ++ly) {
                    for (int lz = 0; lz < 16; ++lz) {
                        class_2680 bs2 = s.method_12254(lx, ly, lz);
                        if (!bs2.method_27852(class_2246.field_10376) && !bs2.method_27852(class_2246.field_10238) || ++count < 30) continue;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean checkAnyRepeaters(class_2818 chunk) {
        int count = 0;
        for (class_2826 s : chunk.method_12006()) {
            if (s == null || s.method_38292() || !s.method_19523(bs -> bs.method_27852(class_2246.field_10450))) continue;
            for (int lx = 0; lx < 16; ++lx) {
                for (int ly = 0; ly < 16; ++ly) {
                    for (int lz = 0; lz < 16; ++lz) {
                        if (!s.method_12254(lx, ly, lz).method_27852(class_2246.field_10450) || ++count < 3) continue;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void notifyFound(String type) {
        this.mc.execute(() -> {
            if (this.mc.field_1724 == null) {
                return;
            }
            if ("repeater".equals(type)) {
                this.mc.field_1724.method_7353((class_2561)class_2561.method_43470((String)"\u00a78\u00a77[TuffChunkFinder] Repeater chunk found!"), false);
            } else {
                int chance = 40 + new Random().nextInt(61);
                this.mc.field_1724.method_7353((class_2561)class_2561.method_43470((String)("\u00a78\u00a77[TuffChunkFinder] Chunk found! \u00a7aBase chance: \u00a7f" + chance + "%")), false);
            }
        });
    }

    private ExecutorService getOrCreateExecutor(String name) {
        if (this.executor == null || this.executor.isShutdown()) {
            this.executor = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, name);
                t.setDaemon(true);
                return t;
            });
        }
        return this.executor;
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (this.mc.field_1687 == null || this.foundChunks.isEmpty()) {
            return;
        }
        for (Map.Entry<class_1923, String> entry : this.foundChunks.entrySet()) {
            class_1923 cp = entry.getKey();
            double x1 = (double)cp.field_9181 * 16.0;
            double z1 = (double)cp.field_9180 * 16.0;
            double x2 = x1 + 16.0;
            double z2 = z1 + 16.0;
            double y1 = 63.0;
            double y2 = y1 + 0.01;
            if ("repeater".equals(entry.getValue())) {
                event.renderer.box(x1, y1, z1, x2, y2, z2, new Color(50, 205, 50, 40), new Color(50, 205, 50, 180), ShapeMode.Both, 0);
                double inner = 8.0;
                event.renderer.box(x1 + inner, y1, z1 + inner, x2 - inner, y2, z2 - inner, new Color(50, 205, 50, 100), new Color(50, 205, 50, 255), ShapeMode.Both, 0);
                continue;
            }
            event.renderer.box(x1, y1, z1, x2, y2, z2, new Color(50, 205, 50, 120), new Color(50, 205, 50, 255), ShapeMode.Both, 0);
        }
    }
}

