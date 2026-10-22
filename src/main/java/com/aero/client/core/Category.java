package com.aero.client.core;

import net.minecraft.class_1799;

/** Module category (the Aero GUI arranges modules into its own panels). */
public class Category {
    public final String name;
    public final class_1799 icon;

    public Category(String name, class_1799 icon) {
        this.name = name;
        this.icon = icon;
    }

    @Override
    public String toString() { return name; }
}
