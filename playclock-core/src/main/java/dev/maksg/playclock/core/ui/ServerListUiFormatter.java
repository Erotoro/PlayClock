package dev.maksg.playclock.core.ui;

import dev.maksg.playclock.core.config.PlayClockConfig;
import dev.maksg.playclock.core.model.TrackedTarget;
import dev.maksg.playclock.core.runtime.PlayClockState;
import dev.maksg.playclock.core.stats.PlaytimeStats;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

public final class ServerListUiFormatter {
    private ServerListUiFormatter() {
    }

    public static ServerListSummarySnapshot createSummary(PlayClockState state, ZoneId zoneId, String minecraftLanguage) {
        return createMultiplayerSummary(state, zoneId, minecraftLanguage);
    }

    public static ServerListSummarySnapshot createMultiplayerSummary(PlayClockState state, ZoneId zoneId, String minecraftLanguage) {
        PlayClockConfig config = state.config();
        long totalSeconds = state.targets().values().stream()
                .filter(TrackedTarget::multiplayer)
                .mapToLong(target -> {
                    PlaytimeStats stats = state.stats().get(target.key());
                    return stats == null ? 0L : stats.totalPlaytimeSeconds();
                })
                .sum();
        return new ServerListSummarySnapshot(TimeFormatters.formatDuration(totalSeconds, config, minecraftLanguage));
    }

    public static ServerListSummarySnapshot createSingleplayerSummary(PlayClockState state, ZoneId zoneId, String minecraftLanguage) {
        PlayClockConfig config = state.config();
        long totalSeconds = state.targets().values().stream()
                .filter(target -> !target.multiplayer())
                .mapToLong(target -> {
                    PlaytimeStats stats = state.stats().get(target.key());
                    return stats == null ? 0L : stats.totalPlaytimeSeconds();
                })
                .sum();
        return new ServerListSummarySnapshot(TimeFormatters.formatDuration(totalSeconds, config, minecraftLanguage));
    }

    public static ServerListDailyBadgeSnapshot createDailyBadge(
            PlaytimeStats stats,
            PlayClockConfig config,
            String minecraftLanguage) {
        long effectiveTodaySeconds = effectiveTodaySeconds(stats, ZoneId.systemDefault());
        if (stats == null || effectiveTodaySeconds <= 0) {
            return null;
        }

        return new ServerListDailyBadgeSnapshot(
                TimeFormatters.formatDuration(effectiveTodaySeconds, config, minecraftLanguage));
    }

    public static ServerListDetailSnapshot createDetail(
            TrackedTarget target,
            PlaytimeStats stats,
            ZoneId zoneId,
            PlayClockConfig config,
            String minecraftLanguage) {
        if (stats == null) {
            return null;
        }

        long effectiveTodaySeconds = effectiveTodaySeconds(stats, zoneId);
        String lastPlayed = formatLastPlayed(stats.lastPlayedAt(), zoneId, minecraftLanguage);

        return new ServerListDetailSnapshot(
                target.displayValue(),
                TimeFormatters.formatDuration(effectiveTodaySeconds, config, minecraftLanguage),
                TimeFormatters.formatDuration(stats.totalPlaytimeSeconds(), config, minecraftLanguage),
                TimeFormatters.formatClockDuration(stats.currentSessionSeconds()),
                lastPlayed);
    }

    private static long effectiveTodaySeconds(PlaytimeStats stats, ZoneId zoneId) {
        if (stats == null) {
            return 0;
        }

        LocalDate today = LocalDate.now(zoneId);
        return today.equals(stats.todayDate()) ? stats.todayPlaytimeSeconds() : 0;
    }

    private static String formatLastPlayed(Instant lastPlayedAt, ZoneId zoneId, String minecraftLanguage) {
        if (lastPlayedAt == null) {
            return "-";
        }

        Locale locale = localeForLanguage(minecraftLanguage);
        LocalDateTime dateTime = LocalDateTime.ofInstant(lastPlayedAt, zoneId);
        LocalDate date = dateTime.toLocalDate();
        LocalDate today = LocalDate.now(zoneId);
        String timePart = normalizeSpaces(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale).format(dateTime));

        if (date.equals(today)) {
            return todayPrefix(minecraftLanguage) + " " + timePart;
        }

        if (date.equals(today.minusDays(1))) {
            return yesterdayPrefix(minecraftLanguage) + " " + timePart;
        }

        return normalizeSpaces(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                .withLocale(locale)
                .format(dateTime));
    }

    private static Locale localeForLanguage(String minecraftLanguage) {
        return switch (minecraftLanguage.toLowerCase(Locale.ROOT)) {
            case "ru_ru" -> Locale.forLanguageTag("ru-RU");
            case "uk_ua" -> Locale.forLanguageTag("uk-UA");
            default -> Locale.US;
        };
    }

    private static String todayPrefix(String minecraftLanguage) {
        return switch (minecraftLanguage.toLowerCase(Locale.ROOT)) {
            case "ru_ru" -> "\u0421\u0435\u0433\u043e\u0434\u043d\u044f";
            case "uk_ua" -> "\u0421\u044c\u043e\u0433\u043e\u0434\u043d\u0456";
            default -> "Today";
        };
    }

    private static String yesterdayPrefix(String minecraftLanguage) {
        return switch (minecraftLanguage.toLowerCase(Locale.ROOT)) {
            case "ru_ru" -> "\u0412\u0447\u0435\u0440\u0430";
            case "uk_ua" -> "\u0412\u0447\u043e\u0440\u0430";
            default -> "Yesterday";
        };
    }

    private static String normalizeSpaces(String value) {
        return value.replace('\u202f', ' ').replace('\u00a0', ' ');
    }
}
