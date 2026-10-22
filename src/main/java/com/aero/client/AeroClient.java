package com.aero.client;

import com.aero.client.api.*;
import com.aero.client.api.Module;
import com.aero.client.gui.AeroScreen;
import com.aero.client.modules.*;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

/** Aero Client - standalone Fabric client (needs only Fabric API). RIGHT SHIFT opens the GUI. */
public class AeroClient implements ClientModInitializer {
    public static final String NAME = "Aero Client";

    @Override
    public void onInitializeClient() {
        for (Module m : new Module[]{
            new AutoTotem(), new AutoLog(), new AutoSprint(), new Fullbright(), new PlayerESP(), new StorageESP(), new AeroScoreboard(),
            new AmethystESP(), new ChunkFinder(), new P3nisFinder(), new PlayerChunkFinder(), new SpawnerChunk(),
            new SpawnerNotifier(), new StorageDensityScanner(), new SusChunkFinder(), new TuffChunkFinder(),
            new SpawnerTags(), new StaffDetector(), new RelogHelper(), new AutoRelog(), new HomeSeter(),
            new AFKFly(), new AutoRTPHome(), new RenderDistanceBypass(), new CustomFreecam(),
            new SpotifyHUD(), new StaffList(), new RegionMap()
        }) Modules.get().add(m);
        Config.load();

        KeyBinding open = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.aero.gui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT,
            KeyBinding.Category.create(Identifier.of("aero", "main"))));

        ClientTickEvents.START_CLIENT_TICK.register(mc -> EventBus.post(TickEvent.Pre.INSTANCE));
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (open.wasPressed()) if (mc.currentScreen == null) mc.setScreen(new AeroScreen());
            EventBus.post(TickEvent.Post.INSTANCE);
            Config.tick();
        });
        ClientChunkEvents.CHUNK_LOAD.register((world, chunk) -> EventBus.post(new ChunkDataEvent(chunk)));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> EventBus.post(new GameLeftEvent()));
        ClientLifecycleEvents.CLIENT_STOPPING.register(c -> Config.save());

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> dispatcher.register(
            ClientCommandManager.literal("aero")
                .then(ClientCommandManager.literal("list").executes(c -> {
                    StringBuilder sb = new StringBuilder("Modules: ");
                    for (Module m : Modules.get().all()) sb.append(m.isActive() ? "\u00a7b" : "\u00a77").append(m.name).append("\u00a7r ");
                    c.getSource().sendFeedback(Text.literal(sb.toString()));
                    return 1;
                }))
                .then(ClientCommandManager.literal("toggle").then(ClientCommandManager.argument("module", StringArgumentType.word()).executes(c -> {
                    Module m = Modules.get().get(StringArgumentType.getString(c, "module"));
                    if (m == null) { c.getSource().sendFeedback(Text.literal("Unknown module.")); return 0; }
                    m.toggle();
                    c.getSource().sendFeedback(Text.literal(m.title + (m.isActive() ? " enabled" : " disabled")));
                    return 1;
                })))
                .then(ClientCommandManager.literal("set").then(ClientCommandManager.argument("module", StringArgumentType.word())
                    .then(ClientCommandManager.argument("setting", StringArgumentType.word())
                        .then(ClientCommandManager.argument("value", StringArgumentType.greedyString()).executes(c -> {
                            Module m = Modules.get().get(StringArgumentType.getString(c, "module"));
                            if (m == null) { c.getSource().sendFeedback(Text.literal("Unknown module.")); return 0; }
                            String sn = StringArgumentType.getString(c, "setting");
                            for (Setting<?> s : m.settings.all()) if (s.name.equalsIgnoreCase(sn)) {
                                boolean ok = s.parse(StringArgumentType.getString(c, "value"));
                                c.getSource().sendFeedback(Text.literal(ok ? s.name + " = " + s.display() : "Invalid value."));
                                return ok ? 1 : 0;
                            }
                            c.getSource().sendFeedback(Text.literal("Unknown setting."));
                            return 0;
                        })))))));
    }
}
