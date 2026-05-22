package dev.maksg.playclock.mc261x.client;

import dev.maksg.playclock.core.identity.ServerIdentityNormalizer;
import dev.maksg.playclock.core.identity.WorldIdentityResolver;
import dev.maksg.playclock.core.model.SourceType;
import dev.maksg.playclock.core.model.TrackedTarget;
import dev.maksg.playclock.core.runtime.PlayClockRuntimeService;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

final class PlayClock261xSessionBridge {
    private final PlayClockRuntimeService runtimeService;
    private String activeTargetKey;

    PlayClock261xSessionBridge(PlayClockRuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    void register() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);
    }

    private void onEndTick(Minecraft client) {
        TrackedTarget currentTarget = detectCurrentTarget(client);
        String nextTargetKey = currentTarget == null ? null : currentTarget.key();

        if (activeTargetKey != null && nextTargetKey == null) {
            runtimeService.stopSession();
            activeTargetKey = null;
            return;
        }

        if (currentTarget != null && !currentTarget.key().equals(activeTargetKey)) {
            if (activeTargetKey != null) {
                runtimeService.stopSession();
            }

            runtimeService.startSession(currentTarget);
            activeTargetKey = currentTarget.key();
            return;
        }

        if (currentTarget != null) {
            runtimeService.flush();
        }
    }

    private TrackedTarget detectCurrentTarget(Minecraft client) {
        if (client.level == null) {
            return null;
        }

        ServerData serverData = client.getCurrentServer();
        if (serverData != null && serverData.ip != null && !serverData.ip.isBlank()) {
            return ServerIdentityNormalizer.normalize(serverData.ip, SourceType.DIRECT);
        }

        MinecraftServer integratedServer = client.getSingleplayerServer();
        if (integratedServer == null) {
            return null;
        }

        String folderName = integratedServer.getWorldPath(LevelResource.ROOT).getFileName().toString();
        String displayName = integratedServer.getWorldData().getLevelName();
        return WorldIdentityResolver.resolve(folderName, displayName, null);
    }
}
