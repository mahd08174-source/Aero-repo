package com.aero.client.core.utils;

import net.minecraft.class_1661;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_310;

/** Hotbar helpers. */
public final class InvUtils {
    private static int previousSlot = -1;

    private InvUtils() {}

    public static FindItemResult findInHotbar(class_1792... items) {
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 == null) return new FindItemResult(-1, 0);
        class_1661 inv = mc.field_1724.method_31548();
        for (int i = 0; i < 9; i++) {
            class_1799 stack = inv.method_5438(i);
            if (stack.method_7960()) continue;
            for (class_1792 item : items) {
                if (stack.method_31574(item)) return new FindItemResult(i, stack.method_7947());
            }
        }
        return new FindItemResult(-1, 0);
    }

    /** Selects a hotbar slot. With {@code swapBack} the previous slot is remembered for {@link #swapBack()}. */
    public static boolean swap(int slot, boolean swapBack) {
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 == null || slot < 0 || slot > 8) return false;
        class_1661 inv = mc.field_1724.method_31548();
        if (swapBack && previousSlot == -1) previousSlot = inv.method_67532();
        else if (!swapBack) previousSlot = -1;
        inv.method_61496(slot);
        return true;
    }

    public static boolean swapBack() {
        if (previousSlot == -1) return false;
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 == null) return false;
        mc.field_1724.method_31548().method_61496(previousSlot);
        previousSlot = -1;
        return true;
    }
}
