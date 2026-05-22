package dev.maksg.playclock.mc261x.client;

import dev.maksg.playclock.core.runtime.PlayClockRuntimeService;
import dev.maksg.playclock.core.ui.PlayClockTranslationKeys;
import java.util.Objects;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.resources.Identifier;

@SuppressWarnings("null")
final class PlayClock261xHotkeyController {
    private static final KeyMapping.Category PLAYCLOCK_CATEGORY = createCategory();

    private final PlayClockRuntimeService runtimeService;
    private final KeyMapping toggleHudKey;
    private final KeyMapping toggleBadgesKey;

    PlayClock261xHotkeyController(PlayClockRuntimeService runtimeService) {
        this.runtimeService = runtimeService;
        this.toggleHudKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                PlayClockTranslationKeys.KEY_TOGGLE_HUD,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                PLAYCLOCK_CATEGORY));
        this.toggleBadgesKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                PlayClockTranslationKeys.KEY_TOGGLE_BADGES,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                PLAYCLOCK_CATEGORY));
    }

    void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleHudKey.consumeClick()) {
                boolean hudEnabled = runtimeService.toggleHud();
                if (client.player != null) {
                    client.player.sendOverlayMessage(
                            Objects.requireNonNull(PlayClock261xLocalization.component(
                                    hudEnabled ? PlayClockTranslationKeys.MESSAGE_HUD_ON : PlayClockTranslationKeys.MESSAGE_HUD_OFF)));
                }
            }

            while (toggleBadgesKey.consumeClick()) {
                boolean badgesEnabled = runtimeService.toggleBadges();
                if (client.player != null) {
                    client.player.sendOverlayMessage(
                            Objects.requireNonNull(PlayClock261xLocalization.component(
                                    badgesEnabled
                                            ? PlayClockTranslationKeys.MESSAGE_BADGES_ON
                                            : PlayClockTranslationKeys.MESSAGE_BADGES_OFF)));
                }
            }
        });
    }

    private static KeyMapping.Category createCategory() {
        return Objects.requireNonNull(new KeyMapping.Category(Identifier.fromNamespaceAndPath("playclock", "keys")));
    }
}
