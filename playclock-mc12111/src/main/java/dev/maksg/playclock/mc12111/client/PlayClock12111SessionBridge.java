package dev.maksg.playclock.mc12111.client;

import dev.maksg.playclock.core.identity.ServerIdentityNormalizer;
import dev.maksg.playclock.core.identity.WorldIdentityResolver;
import dev.maksg.playclock.core.model.SourceType;
import dev.maksg.playclock.core.model.TrackedTarget;
import dev.maksg.playclock.core.runtime.PlayClockRuntimeService;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

final class PlayClock12111SessionBridge {
    private final PlayClockRuntimeService runtimeService;
    private String activeTargetKey;

    PlayClock12111SessionBridge(PlayClockRuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    void register() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);
    }

    private void onEndTick(MinecraftClient client) {
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

    private TrackedTarget detectCurrentTarget(MinecraftClient client) {
        if (client.world == null) {
            return null;
        }

        ServerInfo serverInfo = client.getCurrentServerEntry();
        if (serverInfo != null && serverInfo.address != null && !serverInfo.address.isBlank()) {
            return ServerIdentityNormalizer.normalize(serverInfo.address, SourceType.DIRECT);
        }

        MinecraftServer integratedServer = client.getServer();
        if (integratedServer == null) {
            return null;
        }

        String folderName = integratedServer.getSavePath(WorldSavePath.ROOT).getFileName().toString();
        String displayName = integratedServer.getSaveProperties().getLevelName();
        return WorldIdentityResolver.resolve(folderName, displayName, null);
    }
}
