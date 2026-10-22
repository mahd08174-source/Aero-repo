package com.aero.client.modules;

import com.aero.client.AeroCategory;
import com.aero.client.api.*;
import com.aero.client.api.Module;
import net.minecraft.block.entity.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.List;

public class StorageESP extends Module {
    private final Setting<SettingColor> fill = settings.getDefaultGroup().add(new ColorSetting.Builder()
        .name("fill-color").description("Box fill color.").defaultValue(new SettingColor(255, 166, 26, 35)).build());
    private final Setting<SettingColor> line = settings.getDefaultGroup().add(new ColorSetting.Builder()
        .name("line-color").description("Box outline color.").defaultValue(new SettingColor(255, 166, 26, 255)).build());
    private final Setting<Integer> range = settings.getDefaultGroup().add(new IntSetting.Builder()
        .name("range").description("Scan radius in chunks.").defaultValue(6).min(1).max(16).sliderMin(1).sliderMax(16).build());

    private final List<BlockPos> found = new ArrayList<>();
    private int timer;

    public StorageESP() { super(AeroCategory.HUD_V1, "storage-esp", "Boxes chests, barrels, shulker boxes and hoppers."); }

    @Override public void onDeactivate() { found.clear(); }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.world == null || mc.player == null) { found.clear(); return; }
        if (timer-- > 0) return;
        timer = 20;
        List<BlockPos> next = new ArrayList<>();
        int pcx = mc.player.getBlockPos().getX() >> 4, pcz = mc.player.getBlockPos().getZ() >> 4;
        int r = range.get();
        for (int cx = pcx - r; cx <= pcx + r; cx++)
            for (int cz = pcz - r; cz <= pcz + r; cz++) {
                WorldChunk c = mc.world.getChunkManager().getWorldChunk(cx, cz);
                if (c == null) continue;
                for (BlockEntity be : c.getBlockEntities().values())
                    if (be instanceof ChestBlockEntity || be instanceof BarrelBlockEntity
                        || be instanceof ShulkerBoxBlockEntity || be instanceof HopperBlockEntity) next.add(be.getPos());
            }
        found.clear();
        found.addAll(next);
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        for (BlockPos p : found)
            event.renderer.box(p.getX(), p.getY(), p.getZ(), p.getX() + 1, p.getY() + 1, p.getZ() + 1, fill.get(), line.get(), ShapeMode.Both, 0);
    }
}
