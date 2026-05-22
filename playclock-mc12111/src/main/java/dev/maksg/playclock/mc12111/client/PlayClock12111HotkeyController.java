package dev.maksg.playclock.mc12111.client;

import dev.maksg.playclock.core.runtime.PlayClockRuntimeService;
import dev.maksg.playclock.core.ui.PlayClockTranslationKeys;
import java.util.Objects;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

final class PlayClock12111HotkeyController {
    private static final KeyBinding.Category PLAYCLOCK_CATEGORY =
            Objects.requireNonNull(KeyBinding.Category.create(Identifier.of("playclock", "keys")));
    private final PlayClockRuntimeService runtimeService;
    private final KeyBinding toggleHudKey;
    private final KeyBinding toggleBadgesKey;

    PlayClock12111HotkeyController(PlayClockRuntimeService runtimeService) {
        this.runtimeService = runtimeService;
        this.toggleHudKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                PlayClockTranslationKeys.KEY_TOGGLE_HUD,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                PLAYCLOCK_CATEGORY));
        this.toggleBadgesKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                PlayClockTranslationKeys.KEY_TOGGLE_BADGES,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                PLAYCLOCK_CATEGORY));
    }

    void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleHudKey.wasPressed()) {
                boolean hudEnabled = runtimeService.toggleHud();
                if (client.player != null) {
                    client.player.sendMessage(
                            Objects.requireNonNull(PlayClock12111Localization.text(
                                    hudEnabled ? PlayClockTranslationKeys.MESSAGE_HUD_ON : PlayClockTranslationKeys.MESSAGE_HUD_OFF)),
                            true);
                }
            }

            while (toggleBadgesKey.wasPressed()) {
                boolean badgesEnabled = runtimeService.toggleBadges();
                if (client.player != null) {
                    client.player.sendMessage(
                            Objects.requireNonNull(PlayClock12111Localization.text(
                                    badgesEnabled
                                            ? PlayClockTranslationKeys.MESSAGE_BADGES_ON
                                            : PlayClockTranslationKeys.MESSAGE_BADGES_OFF)),
                            true);
                }
            }
        });
    }
}
