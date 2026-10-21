package com.aero.client.modules;

import com.aero.client.AeroCategory;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1923;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2636;
import net.minecraft.class_2818;
import net.minecraft.class_370;
import net.minecraft.class_374;

public class SpawnerNotifier
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgNotifications;
    private final Setting<Integer> detectionRadius;
    private final Setting<Integer> updateFrequency;
    private final Setting<Boolean> logToChat;
    private final Setting<Boolean> showToasts;
    private final Setting<Boolean> showCoordinates;
    private final Set<class_2338> knownSpawners;
    private int tickCounter;

    public SpawnerNotifier() {
        super(AeroCategory.CATEGORY, "spawner-notifier", "Scans nearby chunks for mob spawners and alerts you via chat or toast notification when a new one is found.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgNotifications = this.settings.createGroup("Notifications");
        this.detectionRadius = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("detection-radius")).description("Chunk radius to scan around you for mob spawners.")).defaultValue(6)).sliderMin(1).sliderMax(16).build());
        this.updateFrequency = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("scan-interval")).description("Ticks between each scan. Lower = more responsive, higher = better performance.")).defaultValue(20)).sliderMin(5).sliderMax(100).build());
        this.logToChat = this.sgNotifications.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("chat-alert")).description("Send a chat message when a new spawner is found.")).defaultValue(true)).build());
        this.showToasts = this.sgNotifications.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("toast-alert")).description("Show a toast notification when a new spawner is found.")).defaultValue(true)).build());
        this.showCoordinates = this.sgNotifications.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("show-coordinates")).description("Include the spawner's XYZ coordinates and distance in the alert message.")).defaultValue(true)).build());
        this.knownSpawners = new HashSet<class_2338>();
        this.tickCounter = 0;
    }

    public void onActivate() {
        this.knownSpawners.clear();
        this.tickCounter = 0;
    }

    public void onDeactivate() {
        this.knownSpawners.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.mc.field_1687 == null || this.mc.field_1724 == null) {
            return;
        }
        if (++this.tickCounter < (Integer)this.updateFrequency.get()) {
            return;
        }
        this.tickCounter = 0;
        class_1923 pc = this.mc.field_1724.method_31476();
        int r = (Integer)this.detectionRadius.get();
        for (int dx = -r; dx <= r; ++dx) {
            for (int dz = -r; dz <= r; ++dz) {
                class_2818 chunk = this.mc.field_1687.method_8497(pc.field_9181 + dx, pc.field_9180 + dz);
                if (chunk == null) continue;
                for (Map.Entry e : chunk.method_12214().entrySet()) {
                    class_2338 pos;
                    if (!(e.getValue() instanceof class_2636) || this.knownSpawners.contains(pos = (class_2338)e.getKey())) continue;
                    this.knownSpawners.add(pos);
                    double dist = new class_243(this.mc.field_1724.method_23317(), this.mc.field_1724.method_23318(), this.mc.field_1724.method_23321()).method_1022(new class_243((double)pos.method_10263() + 0.5, (double)pos.method_10264() + 0.5, (double)pos.method_10260() + 0.5));
                    String coordStr = (Boolean)this.showCoordinates.get() != false ? " at X" + pos.method_10263() + " Y" + pos.method_10264() + " Z" + pos.method_10260() : "";
                    String msg = "Spawner found" + coordStr + " (dist: " + String.format("%.1f", dist) + ")";
                    if (((Boolean)this.logToChat.get()).booleanValue()) {
                        this.mc.field_1724.method_7353((class_2561)class_2561.method_43470((String)("\u00a76[SpawnerNotifier] \u00a7f" + msg)), false);
                    }
                    if (!((Boolean)this.showToasts.get()).booleanValue()) continue;
                    class_370.method_27024((class_374)this.mc.method_1566(), (class_370.class_9037)class_370.class_9037.field_47588, (class_2561)class_2561.method_43470((String)"\u00a76Spawner Found"), (class_2561)class_2561.method_43470((String)msg));
                }
            }
        }
    }
}

