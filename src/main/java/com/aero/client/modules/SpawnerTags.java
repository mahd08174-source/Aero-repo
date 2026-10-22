package com.aero.client.modules;

import com.aero.client.AeroCategory;
import java.lang.reflect.Method;
import com.aero.client.api.Render2DEvent;
import com.aero.client.api.DoubleSetting;
import com.aero.client.api.IntSetting;
import com.aero.client.api.Setting;
import com.aero.client.api.SettingGroup;
import com.aero.client.api.Module;
import com.aero.client.api.Color;
import com.aero.client.api.EventHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.registry.RegistryWrapper;

public class SpawnerTags
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Integer> range;
    private final Setting<Double> textScale;
    private final Setting<Double> yOffset;
    private static final Color ORANGE = new Color(255, 160, 50, 255);

    public SpawnerTags() {
        super(AeroCategory.MISC_V2, "spawner-tags", "Renders a floating label above every nearby mob spawner showing which mob it spawns. Range, text size, and height are all configurable.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.range = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("range")).description("Maximum distance in blocks at which spawner tags are rendered.")).defaultValue(500)).sliderMin(8).sliderMax(500).build());
        this.textScale = this.sgGeneral.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("text-scale")).description("Scale multiplier for the spawner label text. 1.0 is the default size.")).defaultValue(2.6).sliderMin(0.5).sliderMax(3.0).build());
        this.yOffset = this.sgGeneral.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("y-offset")).description("How many blocks above the spawner block the label is drawn.")).defaultValue(0.944).sliderMin(0.5).sliderMax(4.0).build());
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        if (this.mc.player == null || this.mc.world == null) {
            return;
        }
        int r = (Integer)this.range.get();
        BlockPos playerPos = this.mc.player.getBlockPos();
        int chunkR = (r >> 4) + 1;
        int cx = playerPos.getX() >> 4;
        int cz = playerPos.getZ() >> 4;
        for (int dx = -chunkR; dx <= chunkR; ++dx) {
            for (int dz = -chunkR; dz <= chunkR; ++dz) {
                WorldChunk chunk = this.mc.world.getChunkManager().getWorldChunk(cx + dx, cz + dz);
                if (chunk == null) continue;
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    if (!(be instanceof MobSpawnerBlockEntity)) continue;
                    MobSpawnerBlockEntity spawner = (MobSpawnerBlockEntity)be;
                    BlockPos bp = be.getPos();
                    if (playerPos.getSquaredDistance((Vec3i)bp) > (double)((long)r * (long)r)) continue;
                    String mobName = this.getMobName(spawner);
                    String label = "Spawner: " + mobName;
                    double[] sp = com.aero.client.Projection.toScreen((double)bp.getX() + 0.5, (double)bp.getY() + (Double)this.yOffset.get(), (double)bp.getZ() + 0.5);
                    if (sp == null) continue;
                    float scale = (float)(((Double)this.textScale.get()).doubleValue() * 0.5);
                    var ctx = event.drawContext;
                    int w = this.mc.textRenderer.getWidth(label);
                    ctx.getMatrices().pushMatrix();
                    ctx.getMatrices().translate((float)sp[0], (float)sp[1]);
                    ctx.getMatrices().scale(scale, scale);
                    ctx.drawText(this.mc.textRenderer, label, -w / 2, 0, ORANGE.getPacked(), true);
                    ctx.getMatrices().popMatrix();
                }
            }
        }
    }

    private String getMobName(MobSpawnerBlockEntity spawner) {
        try {
            NbtCompound ent;
            NbtElement id;
            NbtCompound sd;
            NbtElement entity;
            NbtCompound nbt = spawner.toInitialChunkDataNbt((RegistryWrapper.WrapperLookup)this.mc.world.getRegistryManager());
            NbtElement spawnData = nbt.get("SpawnData");
            if (spawnData instanceof NbtCompound && (entity = (sd = (NbtCompound)spawnData).get("entity")) instanceof NbtCompound && (id = (ent = (NbtCompound)entity).get("id")) != null) {
                String idStr = id.toString().replace("\"", "");
                return idStr.contains(":") ? this.capitalize(idStr.split(":")[1]) : this.capitalize(idStr);
            }
        }
        catch (Throwable nbt) {
            // empty catch block
        }
        try {
            Object logic = spawner.getClass().getMethod("getLogic", new Class[0]).invoke((Object)spawner, new Object[0]);
            Object entry = null;
            for (Method m : logic.getClass().getMethods()) {
                if (m.getParameterCount() != 0 || !m.getName().contains("Entry")) continue;
                entry = m.invoke(logic, new Object[0]);
                break;
            }
            if (entry != null) {
                for (Method m : entry.getClass().getMethods()) {
                    NbtCompound nc;
                    NbtElement id;
                    Object nbt;
                    if (m.getParameterCount() > 1 || !m.getReturnType().getSimpleName().contains("Nbt")) continue;
                    Object object = nbt = m.getParameterCount() == 0 ? m.invoke(entry, new Object[0]) : null;
                    if (!(nbt instanceof NbtCompound) || (id = (nc = (NbtCompound)nbt).get("id")) == null) continue;
                    String idStr = id.toString().replace("\"", "");
                    return idStr.contains(":") ? this.capitalize(idStr.split(":")[1]) : this.capitalize(idStr);
                }
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return "?";
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).replace("_", " ");
    }
}

