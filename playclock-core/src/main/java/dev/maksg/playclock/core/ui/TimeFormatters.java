package dev.maksg.playclock.core.ui;

import dev.maksg.playclock.core.config.PlayClockConfig;

public final class TimeFormatters {

    private TimeFormatters() {
    }

    public static String formatCompactDuration(long totalSeconds) {
        if (totalSeconds < 60) {
            return totalSeconds + "s";
        }

        long totalMinutes = totalSeconds / 60;
        if (totalMinutes < 60) {
            return totalMinutes + "m";
        }

        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        return hours + "h " + minutes + "m";
    }

    public static String formatClockDuration(long totalSeconds) {
        if (totalSeconds >= 3600) {
            long totalMinutes = totalSeconds / 60;
            long hours = totalMinutes / 60;
            long minutes = totalMinutes % 60;
            return "%02d:%02d".formatted(hours, minutes);
        }

        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return "%02d:%02d".formatted(minutes, seconds);
    }

    public static String formatDuration(long totalSeconds, PlayClockConfig config, String minecraftLanguage) {
        String effectiveLanguage = config.effectiveLanguage(minecraftLanguage);
        return switch (config.timeFormat()) {
            case "clock" -> formatClockDuration(totalSeconds);
            case "localized" -> formatLocalizedCompactDuration(totalSeconds, effectiveLanguage);
            default -> formatCompactDuration(totalSeconds);
        };
    }

    private static String formatLocalizedCompactDuration(long totalSeconds, String language) {
        String secondsSuffix = switch (language) {
            case "ru_ru" -> "\u0441";
            case "uk_ua" -> "\u0441";
            default -> "s";
        };
        String minutesSuffix = switch (language) {
            case "ru_ru" -> "\u043c";
            case "uk_ua" -> "\u0445\u0432";
            default -> "m";
        };
        String hoursSuffix = switch (language) {
            case "ru_ru" -> "\u0447";
            case "uk_ua" -> "\u0433";
            default -> "h";
        };

        if (totalSeconds < 60) {
            return totalSeconds + secondsSuffix;
        }

        long totalMinutes = totalSeconds / 60;
        if (totalMinutes < 60) {
            return totalMinutes + minutesSuffix;
        }

        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        return hours + hoursSuffix + " " + minutes + minutesSuffix;
    }
}
