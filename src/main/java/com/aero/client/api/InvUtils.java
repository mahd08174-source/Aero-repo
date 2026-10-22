package com.aero.client.api;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public final class InvUtils {
    private static int previous = -1;

    public static FindItemResult findInHotbar(Item... items) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return new FindItemResult(-1, 0);
        PlayerInventory inv = mc.player.getInventory();
        for (int i = 0; i < 9; i++) {
            ItemStack s = inv.getStack(i);
            for (Item it : items) if (s.isOf(it)) return new FindItemResult(i, s.getCount());
        }
        return new FindItemResult(-1, 0);
    }

    public static boolean swap(int slot, boolean swapBack) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || slot < 0 || slot > 8) return false;
        PlayerInventory inv = mc.player.getInventory();
        previous = swapBack ? inv.getSelectedSlot() : -1;
        inv.setSelectedSlot(slot);
        return true;
    }

    public static boolean swapBack() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || previous == -1) return false;
        mc.player.getInventory().setSelectedSlot(previous);
        previous = -1;
        return true;
    }
    private InvUtils() {}
}
