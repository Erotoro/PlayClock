package dev.maksg.playclock.mc261x.client;

import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class PlayClock261xLocalization {

    private PlayClock261xLocalization() {
    }

    public static String minecraftLanguage(Minecraft client) {
        return Objects.requireNonNull(client.getLanguageManager().getSelected(), "Minecraft language must not be null");
    }

    public static Component component(String translationKey, Object... args) {
        String safeTranslationKey = Objects.requireNonNull(translationKey, "Translation key must not be null");
        Object[] safeArgs = Objects.requireNonNull(args, "Translation arguments must not be null");
        return Objects.requireNonNull(Component.translatable(safeTranslationKey, safeArgs), "Translated component must not be null");
    }

    public static String string(String translationKey, Object... args) {
        return Objects.requireNonNull(component(translationKey, args).getString(), "Translated string must not be null");
    }
}
