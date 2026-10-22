package com.aero.client.modules;

import com.aero.client.AeroCategory;
import com.aero.client.api.*;
import com.aero.client.api.Module;
import net.minecraft.text.Text;

public class AutoLog extends Module {
    private final Setting<Double> health = settings.getDefaultGroup().add(new DoubleSetting.Builder()
        .name("health").description("Disconnect when your health is at or below this value.")
        .defaultValue(6.0).min(1.0).max(19.0).sliderMin(1.0).sliderMax(19.0).build());

    public AutoLog() { super(AeroCategory.CATEGORY, "auto-log", "Disconnects you from the server when your health gets low."); }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        if (mc.player.getHealth() <= health.get()) {
            disable();
            mc.getNetworkHandler().getConnection().disconnect(Text.literal("[Aero] Auto Log: health <= " + health.get()));
        }
    }
}
