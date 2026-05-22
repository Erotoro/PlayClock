package dev.maksg.playclock.mc12111.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public final class PlayClock12111Localization {

    private PlayClock12111Localization() {
    }

    public static String minecraftLanguage(MinecraftClient client) {
        return client.getLanguageManager().getLanguage();
    }

    public static Text text(String translationKey, Object... args) {
        return Text.translatable(translationKey, args);
    }

    public static String string(String translationKey, Object... args) {
        return text(translationKey, args).getString();
    }
}
