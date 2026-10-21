package com.aero.client.staff;

import net.minecraft.class_2960;

public record StaffEntry(String name, String rankLabel, int color, boolean vanished, int latency, int priority, class_2960 skinTexture) {
    public boolean hasColor() {
        return (this.color & 0xFFFFFF) != 0;
    }
}

