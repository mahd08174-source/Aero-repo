package com.aero.client.modules;

import com.aero.client.AeroCategory;
import com.aero.client.api.*;
import com.aero.client.api.Module;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public class AutoTotem extends Module {
    public AutoTotem() { super(AeroCategory.CATEGORY, "auto-totem", "Moves a totem of undying into your offhand whenever it is empty."); }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        ClientPlayerEntity p = mc.player;
        if (p == null || mc.interactionManager == null || mc.currentScreen != null) return;
        if (p.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING)) return;
        for (int i = 9; i < 45; i++) {
            if (p.playerScreenHandler.getSlot(i).getStack().isOf(Items.TOTEM_OF_UNDYING)) {
                mc.interactionManager.clickSlot(p.playerScreenHandler.syncId, i, 40, SlotActionType.SWAP, p);
                return;
            }
        }
    }
}
