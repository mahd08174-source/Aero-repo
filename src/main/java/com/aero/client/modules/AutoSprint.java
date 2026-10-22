package com.aero.client.modules;

import com.aero.client.AeroCategory;
import com.aero.client.api.*;
import com.aero.client.api.Module;

public class AutoSprint extends Module {
    public AutoSprint() { super(AeroCategory.MISC_V2, "auto-sprint", "Sprints automatically while you move forward."); }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null) return;
        if (mc.options.forwardKey.isPressed() && !mc.player.horizontalCollision && !mc.player.isSneaking()
            && mc.player.getHungerManager().getFoodLevel() > 6) mc.player.setSprinting(true);
    }
}
