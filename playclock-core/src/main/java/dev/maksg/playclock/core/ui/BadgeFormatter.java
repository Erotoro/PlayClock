package dev.maksg.playclock.core.ui;

import dev.maksg.playclock.core.config.PlayClockConfig;
import dev.maksg.playclock.core.stats.PlaytimeStats;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class BadgeFormatter {
    private static final DateTimeFormatter LAST_PLAYED_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private BadgeFormatter() {
    }

    public static BadgeSnapshot create(
            PlaytimeStats stats,
            ZoneId zoneId,
            PlayClockConfig config,
            String minecraftLanguage) {
        String lastPlayed = stats.lastPlayedAt() == null
                ? "-"
                : LAST_PLAYED_FORMATTER.format(stats.lastPlayedAt().atZone(zoneId));

        return new BadgeSnapshot(
                TimeFormatters.formatDuration(stats.totalPlaytimeSeconds(), config, minecraftLanguage),
                TimeFormatters.formatDuration(stats.todayPlaytimeSeconds(), config, minecraftLanguage),
                TimeFormatters.formatClockDuration(stats.currentSessionSeconds()),
                lastPlayed);
    }
}
