package com.aero.client;

import com.aero.client.core.Module;
import com.aero.client.core.ModuleConfig;
import com.aero.client.core.Modules;
import com.aero.client.core.events.ChunkDataEvent;
import com.aero.client.core.events.Events;
import com.aero.client.core.events.GameLeftEvent;
import com.aero.client.core.events.Render2DEvent;
import com.aero.client.core.events.TickEvent;
import com.aero.client.core.render.Render2D;
import com.aero.client.modules.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.class_310;
import org.lwjgl.glfw.GLFW;

/** Aero Client (standalone Fabric client mod). RIGHT SHIFT opens the Aero ClickGUI. */
public class AeroClient implements ClientModInitializer {
    private boolean prevKey = false;

    @Override
    public void onInitializeClient() {
        Modules.registerCategory(AeroCategory.CATEGORY);
        Modules.registerCategory(AeroCategory.MISC_V2);
        Modules.registerCategory(AeroCategory.HUD_V1);

        for (Module m : new Module[]{
            new AmethystESP(), new ChunkFinder(), new P3nisFinder(), new PlayerChunkFinder(), new SpawnerChunk(),
            new SpawnerNotifier(), new StorageDensityScanner(), new SusChunkFinder(), new TuffChunkFinder(),
            new SpawnerTags(), new StaffDetector(), new RelogHelper(), new AutoRelog(), new HomeSeter(),
            new AFKFly(), new AutoRTPHome(), new RenderDistanceBypass(), new CustomFreecam(),
            new SpotifyHUD(), new StaffList(), new RegionMap(), new AeroScoreboard()
        }) Modules.get().add(m);

        ModuleConfig.load();

        ClientTickEvents.START_CLIENT_TICK.register(mc -> {
            pollGuiKey(mc);
            Events.post(TickEvent.Pre.get());
        });
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            Events.post(TickEvent.Post.get());
            ModuleConfig.tick(mc);
        });
        HudRenderCallback.EVENT.register((ctx, tickCounter) -> {
            class_310 mc = class_310.method_1551();
            Render2D.set(ctx);
            try {
                Events.post(new Render2DEvent(ctx, mc.method_22683().method_4486(), mc.method_22683().method_4502(), 0.0, 0.0f));
            } finally {
                Render2D.set(null);
            }
        });
        ClientChunkEvents.CHUNK_LOAD.register((world, chunk) -> Events.post(new ChunkDataEvent(chunk)));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> Events.post(GameLeftEvent.get()));
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> ModuleConfig.save());
    }

    private void pollGuiKey(class_310 mc) {
        if (mc.field_1724 == null) return;
        boolean down = GLFW.glfwGetKey(mc.method_22683().method_4490(), GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
        if (down && !prevKey && mc.field_1755 == null) mc.method_1507(new AeroGui());
        prevKey = down;
    }
}
