package dev.maksg.playclock.core.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.maksg.playclock.core.config.PlayClockConfig;
import org.junit.jupiter.api.Test;

class TimeFormattersTest {

    @Test
    void formatsCompactDurationForHoursMinutesAndSeconds() {
        assertEquals("45s", TimeFormatters.formatCompactDuration(45));
        assertEquals("5m", TimeFormatters.formatCompactDuration(300));
        assertEquals("1h 5m", TimeFormatters.formatCompactDuration(3900));
    }

    @Test
    void formatsClockDurationAsPaddedHoursAndMinutes() {
        assertEquals("00:45", TimeFormatters.formatClockDuration(45));
        assertEquals("05:00", TimeFormatters.formatClockDuration(300));
        assertEquals("01:05", TimeFormatters.formatClockDuration(3900));
    }

    @Test
    void formatsDurationUsingConfiguredFormatAndLanguage() {
        PlayClockConfig compactEnglish = new PlayClockConfig(true, true, true, "compact", "en_us", "top_left");
        PlayClockConfig clock = new PlayClockConfig(true, true, true, "clock", "auto", "top_left");
        PlayClockConfig compactRussian = new PlayClockConfig(true, true, true, "localized", "ru_ru", "top_left");

        assertEquals("1h 5m", TimeFormatters.formatDuration(3900, compactEnglish, "en_us"));
        assertEquals("01:05", TimeFormatters.formatDuration(3900, clock, "en_us"));
        assertEquals("1ч 5м", TimeFormatters.formatDuration(3900, compactRussian, "en_us"));
    }
}
