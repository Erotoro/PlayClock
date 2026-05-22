package dev.maksg.playclock.core.config;

import java.util.Locale;
import java.util.Set;

public record PlayClockConfig(
        boolean hudEnabled,
        boolean badgeEnabled,
        boolean tooltipsEnabled,
        String timeFormat,
        String preferredLanguage,
        String hudAnchor,
        String hudVariant,
        boolean showTodayInHud,
        boolean showSessionInHud,
        boolean showHeaderSummary,
        String tooltipTheme,
        String colorMode,
        String colorPreset,
        int hudLabelColor,
        int hudValueColor,
        int markerLabelColor,
        int markerValueColor,
        int tooltipTitleColor,
        int tooltipLabelColor,
        int tooltipValueColor,
        int headerLabelColor,
        int headerValueColor) {
    private static final Set<String> SUPPORTED_TIME_FORMATS = Set.of("compact", "clock", "localized");
    private static final Set<String> SUPPORTED_LANGUAGES = Set.of("auto", "en_us", "ru_ru", "uk_ua");
    private static final Set<String> SUPPORTED_HUD_ANCHORS = Set.of("top_left", "top_right", "bottom_left", "bottom_right");
    private static final Set<String> SUPPORTED_HUD_VARIANTS = Set.of("compact", "stacked", "minimal");
    private static final Set<String> SUPPORTED_TOOLTIP_THEMES = Set.of("vanilla");
    private static final Set<String> SUPPORTED_COLOR_MODES = Set.of("preset", "custom");
    private static final Set<String> SUPPORTED_COLOR_PRESETS = Set.of("vanilla", "grass", "sunset");

    public PlayClockConfig {
        timeFormat = normalizeTimeFormat(timeFormat);
        preferredLanguage = normalizePreferredLanguage(preferredLanguage);
        hudAnchor = normalizeHudAnchor(hudAnchor);
        hudVariant = normalizeHudVariant(hudVariant);
        tooltipTheme = normalizeTooltipTheme(tooltipTheme);
        colorMode = normalizeColorMode(colorMode);
        colorPreset = normalizeColorPreset(colorPreset);

        PlayClockColorPalette fallbackPalette = PlayClockColorPresets.palette(colorPreset);
        hudLabelColor = normalizeColor(hudLabelColor, fallbackPalette.hudLabelColor());
        hudValueColor = normalizeColor(hudValueColor, fallbackPalette.hudValueColor());
        markerLabelColor = normalizeColor(markerLabelColor, fallbackPalette.markerLabelColor());
        markerValueColor = normalizeColor(markerValueColor, fallbackPalette.markerValueColor());
        tooltipTitleColor = normalizeColor(tooltipTitleColor, fallbackPalette.tooltipTitleColor());
        tooltipLabelColor = normalizeColor(tooltipLabelColor, fallbackPalette.tooltipLabelColor());
        tooltipValueColor = normalizeColor(tooltipValueColor, fallbackPalette.tooltipValueColor());
        headerLabelColor = normalizeColor(headerLabelColor, fallbackPalette.headerLabelColor());
        headerValueColor = normalizeColor(headerValueColor, fallbackPalette.headerValueColor());
    }

    public PlayClockConfig(
            boolean hudEnabled,
            boolean badgeEnabled,
            boolean tooltipsEnabled,
            String timeFormat,
            String preferredLanguage,
            String hudAnchor) {
        this(
                hudEnabled,
                badgeEnabled,
                tooltipsEnabled,
                timeFormat,
                preferredLanguage,
                hudAnchor,
                defaults().hudVariant(),
                defaults().showTodayInHud(),
                defaults().showSessionInHud(),
                defaults().showHeaderSummary(),
                defaults().tooltipTheme(),
                defaults().colorMode(),
                defaults().colorPreset(),
                defaults().hudLabelColor(),
                defaults().hudValueColor(),
                defaults().markerLabelColor(),
                defaults().markerValueColor(),
                defaults().tooltipTitleColor(),
                defaults().tooltipLabelColor(),
                defaults().tooltipValueColor(),
                defaults().headerLabelColor(),
                defaults().headerValueColor());
    }

    public static PlayClockConfig defaults() {
        PlayClockColorPalette palette = PlayClockColorPresets.palette("vanilla");
        return new PlayClockConfig(
                true,
                true,
                true,
                "compact",
                "auto",
                "top_left",
                "compact",
                true,
                true,
                true,
                "vanilla",
                "preset",
                "vanilla",
                palette.hudLabelColor(),
                palette.hudValueColor(),
                palette.markerLabelColor(),
                palette.markerValueColor(),
                palette.tooltipTitleColor(),
                palette.tooltipLabelColor(),
                palette.tooltipValueColor(),
                palette.headerLabelColor(),
                palette.headerValueColor());
    }

    public String effectiveLanguage(String minecraftLanguage) {
        if (!"auto".equals(preferredLanguage)) {
            return preferredLanguage;
        }

        return normalizePreferredLanguage(minecraftLanguage);
    }

    public PlayClockColorPalette colors() {
        if ("preset".equals(colorMode)) {
            return PlayClockColorPresets.palette(colorPreset);
        }

        return new PlayClockColorPalette(
                hudLabelColor,
                hudValueColor,
                markerLabelColor,
                markerValueColor,
                tooltipTitleColor,
                tooltipLabelColor,
                tooltipValueColor,
                headerLabelColor,
                headerValueColor);
    }

    public PlayClockConfig withHudEnabled(boolean enabled) {
        return new PlayClockConfig(
                enabled,
                badgeEnabled,
                tooltipsEnabled,
                timeFormat,
                preferredLanguage,
                hudAnchor,
                hudVariant,
                showTodayInHud,
                showSessionInHud,
                showHeaderSummary,
                tooltipTheme,
                colorMode,
                colorPreset,
                hudLabelColor,
                hudValueColor,
                markerLabelColor,
                markerValueColor,
                tooltipTitleColor,
                tooltipLabelColor,
                tooltipValueColor,
                headerLabelColor,
                headerValueColor);
    }

    public PlayClockConfig withBadgeEnabled(boolean enabled) {
        return new PlayClockConfig(
                hudEnabled,
                enabled,
                tooltipsEnabled,
                timeFormat,
                preferredLanguage,
                hudAnchor,
                hudVariant,
                showTodayInHud,
                showSessionInHud,
                showHeaderSummary,
                tooltipTheme,
                colorMode,
                colorPreset,
                hudLabelColor,
                hudValueColor,
                markerLabelColor,
                markerValueColor,
                tooltipTitleColor,
                tooltipLabelColor,
                tooltipValueColor,
                headerLabelColor,
                headerValueColor);
    }

    private static String normalizeTimeFormat(String value) {
        String normalized = normalize(value, "compact");
        return SUPPORTED_TIME_FORMATS.contains(normalized) ? normalized : "compact";
    }

    private static String normalizePreferredLanguage(String value) {
        String normalized = normalize(value, "auto");
        return SUPPORTED_LANGUAGES.contains(normalized) ? normalized : "auto";
    }

    private static String normalizeHudAnchor(String value) {
        String normalized = normalize(value, "top_left");
        return SUPPORTED_HUD_ANCHORS.contains(normalized) ? normalized : "top_left";
    }

    private static String normalizeHudVariant(String value) {
        String normalized = normalize(value, "compact");
        return SUPPORTED_HUD_VARIANTS.contains(normalized) ? normalized : "compact";
    }

    private static String normalizeTooltipTheme(String value) {
        String normalized = normalize(value, "vanilla");
        return SUPPORTED_TOOLTIP_THEMES.contains(normalized) ? normalized : "vanilla";
    }

    private static String normalizeColorMode(String value) {
        String normalized = normalize(value, "preset");
        return SUPPORTED_COLOR_MODES.contains(normalized) ? normalized : "preset";
    }

    private static String normalizeColorPreset(String value) {
        String normalized = normalize(value, "vanilla");
        return SUPPORTED_COLOR_PRESETS.contains(normalized) ? normalized : "vanilla";
    }

    private static int normalizeColor(int value, int fallback) {
        return value == 0 ? fallback : value;
    }

    private static String normalize(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }

        return value.trim().toLowerCase(Locale.ROOT);
    }
}
