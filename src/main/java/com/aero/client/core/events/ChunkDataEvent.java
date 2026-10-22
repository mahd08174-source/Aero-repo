package com.aero.client.core.events;

import net.minecraft.class_2818;

/** Fired when the client finished loading a chunk received from the server. */
public record ChunkDataEvent(class_2818 chunk) {}
