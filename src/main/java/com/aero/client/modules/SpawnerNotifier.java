package com.aero.client.modules;

import com.aero.client.AeroCategory;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.aero.client.api.TickEvent;
import com.aero.client.api.BoolSetting;
import com.aero.client.api.IntSetting;
import com.aero.client.api.Setting;
import com.aero.client.api.SettingGroup;
import com.aero.client.api.Module;
import com.aero.client.api.EventHandler;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.Text;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.ToastManager;
public class SpawnerNotifier
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgNotifications;
    private final Setting<Integer> detectionRadius;
    private final Setting<Integer> updateFrequency;
    private final Setting<Boolean> logToChat;
    private final Setting<Boolean> showToasts;
    private final Setting<Boolean> showCoordinates;
    private final Set<BlockPos> knownSpawners;
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
        this.knownSpawners = new HashSet<BlockPos>();
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
        if (this.mc.world == null || this.mc.player == null) {
            return;
        }
        if (++this.tickCounter < (Integer)this.updateFrequency.get()) {
            return;
        }
        this.tickCounter = 0;
        ChunkPos pc = this.mc.player.getChunkPos();
        int r = (Integer)this.detectionRadius.get();
        for (int dx = -r; dx <= r; ++dx) {
            for (int dz = -r; dz <= r; ++dz) {
                WorldChunk chunk = this.mc.world.getChunk(pc.x + dx, pc.z + dz);
                if (chunk == null) continue;
                for (Map.Entry e : chunk.getBlockEntities().entrySet()) {
                    BlockPos pos;
                    if (!(e.getValue() instanceof MobSpawnerBlockEntity) || this.knownSpawners.contains(pos = (BlockPos)e.getKey())) continue;
                    this.knownSpawners.add(pos);
                    double dist = new Vec3d(this.mc.player.getX(), this.mc.player.getY(), this.mc.player.getZ()).distanceTo(new Vec3d((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5));
                    String coordStr = (Boolean)this.showCoordinates.get() != false ? " at X" + pos.getX() + " Y" + pos.getY() + " Z" + pos.getZ() : "";
                    String msg = "Spawner found" + coordStr + " (dist: " + String.format("%.1f", dist) + ")";
                    if (((Boolean)this.logToChat.get()).booleanValue()) {
                        this.mc.player.sendMessage((Text)Text.literal((String)("\u00a76[SpawnerNotifier] \u00a7f" + msg)), false);
                    }
                    if (!((Boolean)this.showToasts.get()).booleanValue()) continue;
                    SystemToast.add((ToastManager)this.mc.getToastManager(), (SystemToast.Type)SystemToast.Type.PERIODIC_NOTIFICATION, (Text)Text.literal((String)"\u00a76Spawner Found"), (Text)Text.literal((String)msg));
                }
            }
        }
    }
}

