package dev.maksg.playclock.core.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PlayClockConfigTest {

    @Test
    void normalizesUnknownValuesToSafeDefaults() {
        PlayClockConfig config = new PlayClockConfig(true, true, true, "weird-format", "de_de", "middle");

        assertEquals("compact", config.timeFormat());
        assertEquals("auto", config.preferredLanguage());
        assertEquals("top_left", config.hudAnchor());
    }

    @Test
    void normalizesSupportedValuesCaseInsensitively() {
        PlayClockConfig config = new PlayClockConfig(false, true, false, "CLOCK", "RU_ru", "BOTTOM_RIGHT");

        assertEquals("clock", config.timeFormat());
        assertEquals("ru_ru", config.preferredLanguage());
        assertEquals("bottom_right", config.hudAnchor());
    }

    @Test
    void defaultsExposeFlexibleHudAndColorSettings() {
        PlayClockConfig config = PlayClockConfig.defaults();

        assertEquals("compact", config.hudVariant());
        assertTrue(config.showTodayInHud());
        assertTrue(config.showSessionInHud());
        assertTrue(config.showHeaderSummary());
        assertEquals("vanilla", config.tooltipTheme());
        assertEquals("preset", config.colorMode());
        assertEquals("vanilla", config.colorPreset());
        assertEquals(config.colors().headerValueColor(), config.headerValueColor());
    }
}
