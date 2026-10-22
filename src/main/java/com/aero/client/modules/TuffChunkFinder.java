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
import com.aero.client.api.Render3DEvent;
import com.aero.client.api.TickEvent;
import com.aero.client.api.ShapeMode;
import com.aero.client.api.Module;
import com.aero.client.api.Color;
import com.aero.client.api.EventHandler;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.block.Blocks;
import net.minecraft.block.RepeaterBlock;
import net.minecraft.text.Text;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.block.entity.BeehiveBlockEntity;
public class TuffChunkFinder
extends Module {
    private static final double RENDER_Y = 63.0;
    private static final double CHUNK_THICKNESS = 0.01;
    private static final long FULL_SCAN_INTERVAL = 20000L;
    private static final long QUICK_SCAN_INTERVAL = 200L;
    private static final long COOLDOWN_MS = 50000L;
    private final ConcurrentHashMap<ChunkPos, String> foundChunks = new ConcurrentHashMap();
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
        if (this.mc.world == null || this.mc.player == null) {
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
        if (this.mc.world == null || this.mc.player == null) {
            return;
        }
        ArrayList<ChunkPos> positions = new ArrayList<ChunkPos>();
        ArrayList<WorldChunk> chunks = new ArrayList<WorldChunk>();
        this.collectChunks(positions, chunks);
        this.getOrCreateExecutor("tuff-quick-scan").submit(() -> {
            for (int i = 0; i < positions.size(); ++i) {
                if (this.countPoweredRepeaters((WorldChunk)chunks.get(i)) < 3) continue;
                this.foundChunks.clear();
                this.foundChunks.put((ChunkPos)positions.get(i), "repeater");
                return;
            }
        });
    }

    private void fullScan() {
        if (this.mc.world == null || this.mc.player == null) {
            this.scanning.set(false);
            return;
        }
        ArrayList<ChunkPos> positions = new ArrayList<ChunkPos>();
        ArrayList<WorldChunk> chunks = new ArrayList<WorldChunk>();
        this.collectChunks(positions, chunks);
        this.getOrCreateExecutor("tuff-full-scan").submit(() -> {
            try {
                int i;
                for (i = 0; i < positions.size(); ++i) {
                    if (this.countPoweredRepeaters((WorldChunk)chunks.get(i)) < 3) continue;
                    this.lastMarkTime = System.currentTimeMillis();
                    this.foundChunks.clear();
                    this.foundChunks.put((ChunkPos)positions.get(i), "repeater");
                    this.notifyFound("repeater");
                    return;
                }
                for (i = 0; i < positions.size(); ++i) {
                    String type;
                    boolean hasRepeater;
                    WorldChunk chunk = (WorldChunk)chunks.get(i);
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
                        this.foundChunks.put((ChunkPos)positions.get(i), type);
                        this.notifyFound("repeater");
                    } else if (System.currentTimeMillis() - this.lastMarkTime >= 50000L) {
                        this.lastMarkTime = System.currentTimeMillis();
                        this.foundChunks.clear();
                        this.foundChunks.put((ChunkPos)positions.get(i), type);
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

    private void collectChunks(List<ChunkPos> positions, List<WorldChunk> chunks) {
        ChunkPos pc = this.mc.player.getChunkPos();
        int dist = Math.min((Integer)this.mc.options.getViewDistance().getValue(), 8);
        for (int dx = -dist; dx <= dist; ++dx) {
            for (int dz = -dist; dz <= dist; ++dz) {
                ChunkPos cp = new ChunkPos(pc.x + dx, pc.z + dz);
                WorldChunk chunk = this.mc.world.getChunkManager().getWorldChunk(cp.x, cp.z);
                if (chunk == null || chunk.isEmpty()) continue;
                positions.add(cp);
                chunks.add(chunk);
            }
        }
    }

    private int countPoweredRepeaters(WorldChunk chunk) {
        int count = 0;
        for (ChunkSection section : chunk.getSectionArray()) {
            if (section == null || section.isEmpty() || !section.hasAny(bs -> bs.isOf(Blocks.REPEATER))) continue;
            for (int lx = 0; lx < 16; ++lx) {
                for (int ly = 0; ly < 16; ++ly) {
                    for (int lz = 0; lz < 16; ++lz) {
                        BlockState bs2 = section.getBlockState(lx, ly, lz);
                        if (!bs2.isOf(Blocks.REPEATER) || !((Boolean)bs2.get((Property)RepeaterBlock.POWERED)).booleanValue() || ++count < 3) continue;
                        return count;
                    }
                }
            }
        }
        return count;
    }

    private boolean checkHive(WorldChunk chunk) {
        for (BlockEntity be : chunk.getBlockEntities().values()) {
            BeehiveBlockEntity hive;
            if (!(be instanceof BeehiveBlockEntity) || (hive = (BeehiveBlockEntity)be).getBeeCount() <= 0) continue;
            return true;
        }
        return false;
    }

    private boolean checkCobbledDeepslate(WorldChunk chunk) {
        int sectionY;
        int count = 0;
        int bottomY = chunk.getBottomY();
        ChunkSection[] sections = chunk.getSectionArray();
        for (int si = 0; si < sections.length && (sectionY = bottomY + si * 16) <= 20; ++si) {
            ChunkSection s;
            if (sectionY + 16 < 0 || (s = sections[si]) == null || s.isEmpty() || !s.hasAny(bs -> bs.isOf(Blocks.COBBLED_DEEPSLATE))) continue;
            for (int lx = 0; lx < 16; ++lx) {
                for (int ly = 0; ly < 16; ++ly) {
                    for (int lz = 0; lz < 16; ++lz) {
                        int wy = sectionY + ly;
                        if (wy < 0 || wy > 20 || !s.getBlockState(lx, ly, lz).isOf(Blocks.COBBLED_DEEPSLATE) || ++count < 50) continue;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean checkVines(WorldChunk chunk) {
        int count = 0;
        for (ChunkSection s : chunk.getSectionArray()) {
            if (s == null || s.isEmpty() || !s.hasAny(bs -> bs.isOf(Blocks.VINE))) continue;
            for (int lx = 0; lx < 16; ++lx) {
                for (int ly = 0; ly < 16; ++ly) {
                    for (int lz = 0; lz < 16; ++lz) {
                        if (!s.getBlockState(lx, ly, lz).isOf(Blocks.VINE) || ++count < 150) continue;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean checkSeagrass(WorldChunk chunk) {
        int sectionY;
        int count = 0;
        int bottomY = chunk.getBottomY();
        ChunkSection[] sections = chunk.getSectionArray();
        for (int si = 0; si < sections.length && (sectionY = bottomY + si * 16) <= 70; ++si) {
            ChunkSection s = sections[si];
            if (s == null || s.isEmpty() || !s.hasAny(bs -> bs.isOf(Blocks.SEAGRASS) || bs.isOf(Blocks.TALL_SEAGRASS))) continue;
            for (int lx = 0; lx < 16; ++lx) {
                for (int ly = 0; ly < 16; ++ly) {
                    for (int lz = 0; lz < 16; ++lz) {
                        BlockState bs2 = s.getBlockState(lx, ly, lz);
                        if (!bs2.isOf(Blocks.SEAGRASS) && !bs2.isOf(Blocks.TALL_SEAGRASS) || ++count < 30) continue;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean checkAnyRepeaters(WorldChunk chunk) {
        int count = 0;
        for (ChunkSection s : chunk.getSectionArray()) {
            if (s == null || s.isEmpty() || !s.hasAny(bs -> bs.isOf(Blocks.REPEATER))) continue;
            for (int lx = 0; lx < 16; ++lx) {
                for (int ly = 0; ly < 16; ++ly) {
                    for (int lz = 0; lz < 16; ++lz) {
                        if (!s.getBlockState(lx, ly, lz).isOf(Blocks.REPEATER) || ++count < 3) continue;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void notifyFound(String type) {
        this.mc.execute(() -> {
            if (this.mc.player == null) {
                return;
            }
            if ("repeater".equals(type)) {
                this.mc.player.sendMessage((Text)Text.literal((String)"\u00a78\u00a77[TuffChunkFinder] Repeater chunk found!"), false);
            } else {
                int chance = 40 + new Random().nextInt(61);
                this.mc.player.sendMessage((Text)Text.literal((String)("\u00a78\u00a77[TuffChunkFinder] Chunk found! \u00a7aBase chance: \u00a7f" + chance + "%")), false);
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
        if (this.mc.world == null || this.foundChunks.isEmpty()) {
            return;
        }
        for (Map.Entry<ChunkPos, String> entry : this.foundChunks.entrySet()) {
            ChunkPos cp = entry.getKey();
            double x1 = (double)cp.x * 16.0;
            double z1 = (double)cp.z * 16.0;
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

