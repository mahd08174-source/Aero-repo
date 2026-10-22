package com.aero.client.staff;

import com.aero.client.modules.StaffList;
import com.aero.client.staff.StaffDetector;
import com.aero.client.staff.StaffEntry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import net.minecraft.world.GameMode;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.SkinTextures;
public final class StaffTracker {
    private static final int SCAN_INTERVAL = 10;
    private final StaffList module;
    private volatile List<StaffEntry> current = List.of();
    private Set<String> lastNames = new HashSet<String>();
    private boolean primed;
    private int ticks;

    public StaffTracker(StaffList module) {
        this.module = module;
    }

    public List<StaffEntry> current() {
        return this.current;
    }

    public void reset() {
        this.lastNames = new HashSet<String>();
        this.primed = false;
        this.current = List.of();
        this.ticks = 0;
    }

    public void clear() {
        this.reset();
    }

    public void tick() {
        if (++this.ticks % 10 != 0) {
            return;
        }
        this.scan();
    }

    private void scan() {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayNetworkHandler conn = mc.getNetworkHandler();
        if (conn == null || mc.player == null) {
            this.current = List.of();
            this.lastNames = new HashSet<String>();
            this.primed = false;
            return;
        }
        StaffDetector.DetectConfig cfg = this.module.detectConfig();
        UUID self = mc.player.getUuid();
        HashSet inWorld = new HashSet();
        if (mc.world != null) {
            mc.world.getPlayers().forEach(p -> inWorld.add(p.getUuid()));
        }
        ArrayList<StaffEntry> found = new ArrayList<StaffEntry>();
        for (PlayerListEntry info : conn.getPlayerList()) {
            boolean vanished;
            String name;
            UUID id = info.getProfile().id();
            if (id.equals(self) || (name = info.getProfile().name()) == null || name.isEmpty()) continue;
            boolean bl = vanished = info.getGameMode() == GameMode.SPECTATOR || !inWorld.contains(id);
            if (vanished && !cfg.showVanished()) continue;
            Team team = info.getScoreboardTeam();
            StaffEntry entry = StaffDetector.classify(name, info.getDisplayName(), team != null ? team.getPrefix() : null, team != null ? team.getSuffix() : null, team != null ? team.getName() : null, vanished, info.getLatency(), cfg);
            if (entry == null) continue;
            Identifier skin = null;
            try {
                SkinTextures textures = info.getSkinTextures();
                if (textures != null && textures.body() != null) {
                    skin = textures.body().texturePath();
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            found.add(new StaffEntry(entry.name(), entry.rankLabel(), entry.color(), entry.vanished(), entry.latency(), entry.priority(), skin));
        }
        found.sort(Comparator.comparing(StaffEntry::vanished).thenComparing(Comparator.comparingInt(StaffEntry::priority).reversed()).thenComparing(e -> e.name().toLowerCase(Locale.ROOT)));
        HashSet<String> names = new HashSet<String>();
        for (StaffEntry e2 : found) {
            names.add(e2.name());
        }
        if (this.primed) {
            for (StaffEntry e2 : found) {
                if (this.lastNames.contains(e2.name())) continue;
                this.module.onStaffAppear(e2);
            }
        }
        this.lastNames = names;
        this.primed = true;
        this.current = List.copyOf(found);
    }
}

