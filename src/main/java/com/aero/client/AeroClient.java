package com.aero.client;

import com.aero.client.modules.*;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.screens.ModulesScreen;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.class_310;
import org.lwjgl.glfw.GLFW;

/** Aero Client. RIGHT SHIFT opens the Aero ClickGUI (replacing Meteor's own GUI on that key). */
public class AeroClient extends MeteorAddon {
    private boolean prevKey = false;

    @Override
    public void onInitialize() {
        MeteorClient.EVENT_BUS.subscribe(this);   // lambda factory for this package is registered by Meteor via getPackage()

        for (Module m : new Module[]{
            new AmethystESP(), new ChunkFinder(), new P3nisFinder(), new PlayerChunkFinder(), new SpawnerChunk(),
            new SpawnerNotifier(), new StorageDensityScanner(), new SusChunkFinder(), new TuffChunkFinder(),
            new SpawnerTags(), new StaffDetector(), new RelogHelper(), new AutoRelog(), new HomeSeter(),
            new AFKFly(), new AutoRTPHome(), new RenderDistanceBypass(), new CustomFreecam(),
            new SpotifyHUD(), new StaffList(), new RegionMap(), new AeroScoreboard()
        }) Modules.get().add(m);
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(AeroCategory.CATEGORY);
        Modules.registerCategory(AeroCategory.MISC_V2);
        Modules.registerCategory(AeroCategory.HUD_V1);
    }

    @Override
    public String getPackage() { return "com.aero.client"; }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onTick(TickEvent.Pre event) {
        class_310 mc = MeteorClient.mc;
        if (mc.field_1724 == null) return;
        boolean down = GLFW.glfwGetKey(mc.method_22683().method_4490(), GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
        if (mc.field_1755 instanceof ModulesScreen) mc.method_1507(new AeroGui());
        else if (down && !prevKey && mc.field_1755 == null) mc.method_1507(new AeroGui());
        prevKey = down;
    }
}
