package com.aero.client.api;

import net.minecraft.world.chunk.WorldChunk;

public class ChunkDataEvent {
    public final WorldChunk chunk;
    public ChunkDataEvent(WorldChunk chunk) { this.chunk = chunk; }
    public WorldChunk chunk() { return chunk; }
}
