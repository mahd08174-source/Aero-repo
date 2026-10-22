package com.aero.client.modules;

import com.aero.client.AeroCategory;
import java.lang.reflect.Method;
import com.aero.client.core.events.Render2DEvent;
import com.aero.client.core.render.TextRenderer;
import com.aero.client.core.settings.DoubleSetting;
import com.aero.client.core.settings.IntSetting;
import com.aero.client.core.settings.Setting;
import com.aero.client.core.settings.SettingGroup;
import com.aero.client.core.Module;
import com.aero.client.core.render.NametagUtils;
import com.aero.client.core.render.Color;
import com.aero.client.core.events.EventHandler;
import net.minecraft.class_2338;
import net.minecraft.class_2382;
import net.minecraft.class_2487;
import net.minecraft.class_2520;
import net.minecraft.class_2586;
import net.minecraft.class_2636;
import net.minecraft.class_2818;
import net.minecraft.class_7225;
import org.joml.Vector3d;

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
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
            return;
        }
        int r = (Integer)this.range.get();
        class_2338 playerPos = this.mc.field_1724.method_24515();
        int chunkR = (r >> 4) + 1;
        int cx = playerPos.method_10263() >> 4;
        int cz = playerPos.method_10260() >> 4;
        Vector3d pos = new Vector3d();
        for (int dx = -chunkR; dx <= chunkR; ++dx) {
            for (int dz = -chunkR; dz <= chunkR; ++dz) {
                class_2818 chunk = this.mc.field_1687.method_2935().method_21730(cx + dx, cz + dz);
                if (chunk == null) continue;
                for (class_2586 be : chunk.method_12214().values()) {
                    if (!(be instanceof class_2636)) continue;
                    class_2636 spawner = (class_2636)be;
                    class_2338 bp = be.method_11016();
                    if (playerPos.method_10262((class_2382)bp) > (double)((long)r * (long)r)) continue;
                    String mobName = this.getMobName(spawner);
                    String label = "Spawner: " + mobName;
                    pos.set((double)bp.method_10263() + 0.5, (double)bp.method_10264() + (Double)this.yOffset.get(), (double)bp.method_10260() + 0.5);
                    if (!NametagUtils.to2D((Vector3d)pos, (double)1.0)) continue;
                    double scale = (Double)this.textScale.get();
                    TextRenderer tr = TextRenderer.get();
                    NametagUtils.begin((Vector3d)pos);
                    tr.begin(scale, false, true);
                    double w = tr.getWidth(label);
                    tr.render(label, (double)((float)(-w / 2.0)), 0.0, ORANGE, true);
                    tr.end();
                    NametagUtils.end();
                }
            }
        }
    }

    private String getMobName(class_2636 spawner) {
        try {
            class_2487 ent;
            class_2520 id;
            class_2487 sd;
            class_2520 entity;
            class_2487 nbt = spawner.method_16887((class_7225.class_7874)this.mc.field_1687.method_30349());
            class_2520 spawnData = nbt.method_10580("SpawnData");
            if (spawnData instanceof class_2487 && (entity = (sd = (class_2487)spawnData).method_10580("entity")) instanceof class_2487 && (id = (ent = (class_2487)entity).method_10580("id")) != null) {
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
                    class_2487 nc;
                    class_2520 id;
                    Object nbt;
                    if (m.getParameterCount() > 1 || !m.getReturnType().getSimpleName().contains("Nbt")) continue;
                    Object object = nbt = m.getParameterCount() == 0 ? m.invoke(entry, new Object[0]) : null;
                    if (!(nbt instanceof class_2487) || (id = (nc = (class_2487)nbt).method_10580("id")) == null) continue;
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

