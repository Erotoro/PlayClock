package dev.maksg.playclock.mc12111.client;

import dev.maksg.playclock.core.runtime.PlayClockRuntimeFactory;
import dev.maksg.playclock.core.runtime.PlayClockRuntimeService;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PlayClock12111Client implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("PlayClock/1.21.11/Client");
    private static PlayClockRuntimeService runtimeService;
    private static PlayClock12111SessionBridge sessionBridge;
    private static PlayClock12111HudOverlay hudOverlay;
    private static PlayClock12111HotkeyController hotkeyController;

    @Override
    public void onInitializeClient() {
        runtimeService = createRuntimeService();
        registerShutdownHook(runtimeService);
        sessionBridge = new PlayClock12111SessionBridge(runtimeService);
        sessionBridge.register();
        hudOverlay = new PlayClock12111HudOverlay(runtimeService);
        hudOverlay.register();
        hotkeyController = new PlayClock12111HotkeyController(runtimeService);
        hotkeyController.register();
        LOGGER.info("PlayClock client runtime is ready for Minecraft 1.21.11");
    }

    public static PlayClockRuntimeService runtimeService() {
        return runtimeService;
    }

    private static PlayClockRuntimeService createRuntimeService() {
        Path dataFile = FabricLoader.getInstance()
                .getConfigDir()
                .resolve("playclock")
                .resolve("playclock-state.json");

        try {
            return PlayClockRuntimeFactory.create(dataFile);
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to initialize PlayClock runtime for 1.21.11", exception);
        }
    }

    private static void registerShutdownHook(PlayClockRuntimeService runtimeService) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                runtimeService.shutdown();
            } catch (RuntimeException exception) {
                LOGGER.error("Failed to durably persist PlayClock state during shutdown", exception);
            }
        }, "PlayClock-12111-Shutdown"));
    }
}
