package dev.maksg.playclock.core.ui;

import dev.maksg.playclock.core.config.PlayClockConfig;
import dev.maksg.playclock.core.model.TrackedTarget;
import dev.maksg.playclock.core.stats.PlaytimeStats;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public final class HudFormatter {

    private HudFormatter() {
    }

    public static HudSnapshot create(
            TrackedTarget target,
            PlaytimeStats stats,
            PlayClockConfig config,
            String minecraftLanguage,
            ZoneId zoneId) {
        if (target == null || stats == null) {
            return null;
        }

        List<HudMetric> metrics = new ArrayList<>();
        metrics.add(new HudMetric(
                PlayClockTranslationKeys.LABEL_TOTAL,
                TimeFormatters.formatDuration(stats.totalPlaytimeSeconds(), config, minecraftLanguage)));

        if ("minimal".equals(config.hudVariant())) {
            return new HudSnapshot(config.hudVariant(), target.displayValue(), List.copyOf(metrics));
        }

        if (config.showTodayInHud()) {
            metrics.add(new HudMetric(
                    PlayClockTranslationKeys.LABEL_TODAY,
                    TimeFormatters.formatDuration(effectiveTodaySeconds(stats, zoneId), config, minecraftLanguage)));
        }

        if (config.showSessionInHud()) {
            metrics.add(new HudMetric(
                    PlayClockTranslationKeys.LABEL_SESSION,
                    TimeFormatters.formatClockDuration(stats.currentSessionSeconds())));
        }

        return new HudSnapshot(config.hudVariant(), target.displayValue(), List.copyOf(metrics));
    }

    private static long effectiveTodaySeconds(PlaytimeStats stats, ZoneId zoneId) {
        LocalDate today = LocalDate.now(zoneId);
        return today.equals(stats.todayDate()) ? stats.todayPlaytimeSeconds() : 0;
    }
}
