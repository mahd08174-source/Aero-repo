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
import net.minecraft.class_1934;
import net.minecraft.class_268;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_634;
import net.minecraft.class_640;
import net.minecraft.class_8685;

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
        class_310 mc = class_310.method_1551();
        class_634 conn = mc.method_1562();
        if (conn == null || mc.field_1724 == null) {
            this.current = List.of();
            this.lastNames = new HashSet<String>();
            this.primed = false;
            return;
        }
        StaffDetector.DetectConfig cfg = this.module.detectConfig();
        UUID self = mc.field_1724.method_5667();
        HashSet inWorld = new HashSet();
        if (mc.field_1687 != null) {
            mc.field_1687.method_18456().forEach(p -> inWorld.add(p.method_5667()));
        }
        ArrayList<StaffEntry> found = new ArrayList<StaffEntry>();
        for (class_640 info : conn.method_2880()) {
            boolean vanished;
            String name;
            UUID id = info.method_2966().id();
            if (id.equals(self) || (name = info.method_2966().name()) == null || name.isEmpty()) continue;
            boolean bl = vanished = info.method_2958() == class_1934.field_9219 || !inWorld.contains(id);
            if (vanished && !cfg.showVanished()) continue;
            class_268 team = info.method_2955();
            StaffEntry entry = StaffDetector.classify(name, info.method_2971(), team != null ? team.method_1144() : null, team != null ? team.method_1136() : null, team != null ? team.method_1197() : null, vanished, info.method_2959(), cfg);
            if (entry == null) continue;
            class_2960 skin = null;
            try {
                class_8685 textures = info.method_52810();
                if (textures != null && textures.comp_1626() != null) {
                    skin = textures.comp_1626().comp_3627();
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

