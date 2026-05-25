package dev.maksg.playclock.mc261x.client;

import dev.maksg.playclock.core.runtime.PlayClockRuntimeFactory;
import dev.maksg.playclock.core.runtime.PlayClockRuntimeService;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PlayClock261xClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("PlayClock/26.1.x/Client");
    private static PlayClockRuntimeService runtimeService;
    private static PlayClock261xSessionBridge sessionBridge;
    private static PlayClock261xHudOverlay hudOverlay;
    private static PlayClock261xHotkeyController hotkeyController;

    @Override
    public void onInitializeClient() {
        runtimeService = createRuntimeService();
        registerShutdownHook(runtimeService);
        sessionBridge = new PlayClock261xSessionBridge(runtimeService);
        sessionBridge.register();
        hudOverlay = new PlayClock261xHudOverlay(runtimeService);
        hudOverlay.register();
        hotkeyController = new PlayClock261xHotkeyController(runtimeService);
        hotkeyController.register();
        LOGGER.info("PlayClock client runtime is ready for Minecraft 26.1.x");
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
            throw new UncheckedIOException("Failed to initialize PlayClock runtime for 26.1.x", exception);
        }
    }

    private static void registerShutdownHook(PlayClockRuntimeService runtimeService) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                runtimeService.shutdown();
            } catch (RuntimeException exception) {
                LOGGER.error("Failed to durably persist PlayClock state during shutdown", exception);
            }
        }, "PlayClock-261x-Shutdown"));
    }
}
