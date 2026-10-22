package com.aero.client.staff;

import net.minecraft.util.Identifier;
public record StaffEntry(String name, String rankLabel, int color, boolean vanished, int latency, int priority, Identifier skinTexture) {
    public boolean hasColor() {
        return (this.color & 0xFFFFFF) != 0;
    }
}

