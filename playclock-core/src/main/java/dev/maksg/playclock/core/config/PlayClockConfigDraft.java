package dev.maksg.playclock.core.config;

public final class PlayClockConfigDraft {
    private boolean hudEnabled;
    private boolean badgeEnabled;
    private boolean tooltipsEnabled;
    private String timeFormat;
    private String preferredLanguage;
    private String hudAnchor;
    private String hudVariant;
    private boolean showTodayInHud;
    private boolean showSessionInHud;
    private boolean showHeaderSummary;
    private String tooltipTheme;
    private String colorMode;
    private String colorPreset;
    private int hudLabelColor;
    private int hudValueColor;
    private int markerLabelColor;
    private int markerValueColor;
    private int tooltipTitleColor;
    private int tooltipLabelColor;
    private int tooltipValueColor;
    private int headerLabelColor;
    private int headerValueColor;

    private PlayClockConfigDraft() {
    }

    public static PlayClockConfigDraft from(PlayClockConfig config) {
        PlayClockConfigDraft draft = new PlayClockConfigDraft();
        draft.hudEnabled = config.hudEnabled();
        draft.badgeEnabled = config.badgeEnabled();
        draft.tooltipsEnabled = config.tooltipsEnabled();
        draft.timeFormat = config.timeFormat();
        draft.preferredLanguage = config.preferredLanguage();
        draft.hudAnchor = config.hudAnchor();
        draft.hudVariant = config.hudVariant();
        draft.showTodayInHud = config.showTodayInHud();
        draft.showSessionInHud = config.showSessionInHud();
        draft.showHeaderSummary = config.showHeaderSummary();
        draft.tooltipTheme = config.tooltipTheme();
        draft.colorMode = config.colorMode();
        draft.colorPreset = config.colorPreset();
        draft.hudLabelColor = config.hudLabelColor();
        draft.hudValueColor = config.hudValueColor();
        draft.markerLabelColor = config.markerLabelColor();
        draft.markerValueColor = config.markerValueColor();
        draft.tooltipTitleColor = config.tooltipTitleColor();
        draft.tooltipLabelColor = config.tooltipLabelColor();
        draft.tooltipValueColor = config.tooltipValueColor();
        draft.headerLabelColor = config.headerLabelColor();
        draft.headerValueColor = config.headerValueColor();
        return draft;
    }

    public PlayClockConfig toConfig() {
        return new PlayClockConfig(
                hudEnabled,
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

    public PlayClockColorPalette effectiveColors() {
        return toConfig().colors();
    }

    public int effectiveColor(PlayClockColorSlot slot) {
        PlayClockColorPalette palette = effectiveColors();
        return switch (slot) {
            case HUD_LABEL -> palette.hudLabelColor();
            case HUD_VALUE -> palette.hudValueColor();
            case MARKER_LABEL -> palette.markerLabelColor();
            case MARKER_VALUE -> palette.markerValueColor();
            case TOOLTIP_TITLE -> palette.tooltipTitleColor();
            case TOOLTIP_LABEL -> palette.tooltipLabelColor();
            case TOOLTIP_VALUE -> palette.tooltipValueColor();
            case HEADER_LABEL -> palette.headerLabelColor();
            case HEADER_VALUE -> palette.headerValueColor();
        };
    }

    public boolean hudEnabled() {
        return hudEnabled;
    }

    public void setHudEnabled(boolean hudEnabled) {
        this.hudEnabled = hudEnabled;
    }

    public boolean badgeEnabled() {
        return badgeEnabled;
    }

    public void setBadgeEnabled(boolean badgeEnabled) {
        this.badgeEnabled = badgeEnabled;
    }

    public boolean tooltipsEnabled() {
        return tooltipsEnabled;
    }

    public void setTooltipsEnabled(boolean tooltipsEnabled) {
        this.tooltipsEnabled = tooltipsEnabled;
    }

    public String timeFormat() {
        return timeFormat;
    }

    public void nextTimeFormat() {
        this.timeFormat = switch (timeFormat) {
            case "clock" -> "localized";
            case "localized" -> "compact";
            default -> "clock";
        };
    }

    public String preferredLanguage() {
        return preferredLanguage;
    }

    public void nextLanguage() {
        this.preferredLanguage = switch (preferredLanguage) {
            case "en_us" -> "ru_ru";
            case "ru_ru" -> "uk_ua";
            case "uk_ua" -> "auto";
            default -> "en_us";
        };
    }

    public String hudAnchor() {
        return hudAnchor;
    }

    public void nextHudAnchor() {
        this.hudAnchor = switch (hudAnchor) {
            case "top_right" -> "bottom_right";
            case "bottom_right" -> "bottom_left";
            case "bottom_left" -> "top_left";
            default -> "top_right";
        };
    }

    public String hudVariant() {
        return hudVariant;
    }

    public void nextHudVariant() {
        this.hudVariant = switch (hudVariant) {
            case "stacked" -> "minimal";
            case "minimal" -> "compact";
            default -> "stacked";
        };
    }

    public boolean showTodayInHud() {
        return showTodayInHud;
    }

    public void setShowTodayInHud(boolean showTodayInHud) {
        this.showTodayInHud = showTodayInHud;
    }

    public boolean showSessionInHud() {
        return showSessionInHud;
    }

    public void setShowSessionInHud(boolean showSessionInHud) {
        this.showSessionInHud = showSessionInHud;
    }

    public boolean showHeaderSummary() {
        return showHeaderSummary;
    }

    public void setShowHeaderSummary(boolean showHeaderSummary) {
        this.showHeaderSummary = showHeaderSummary;
    }

    public String colorMode() {
        return colorMode;
    }

    public void setColorMode(String colorMode) {
        this.colorMode = colorMode;
    }

    public void nextColorMode() {
        this.colorMode = "preset".equals(colorMode) ? "custom" : "preset";
    }

    public String colorPreset() {
        return colorPreset;
    }

    public void setColorPreset(String colorPreset) {
        this.colorPreset = colorPreset;
    }

    public void nextColorPreset() {
        this.colorPreset = switch (colorPreset) {
            case "grass" -> "sunset";
            case "sunset" -> "vanilla";
            default -> "grass";
        };
    }

    public void nextColorPresetAndApply() {
        nextColorPreset();
        usePresetColors();
    }

    public void usePresetColors() {
        colorMode = "preset";
        applyPresetColors();
    }

    public void applyPresetColors() {
        PlayClockColorPalette palette = PlayClockColorPresets.palette(colorPreset);
        hudLabelColor = palette.hudLabelColor();
        hudValueColor = palette.hudValueColor();
        markerLabelColor = palette.markerLabelColor();
        markerValueColor = palette.markerValueColor();
        tooltipTitleColor = palette.tooltipTitleColor();
        tooltipLabelColor = palette.tooltipLabelColor();
        tooltipValueColor = palette.tooltipValueColor();
        headerLabelColor = palette.headerLabelColor();
        headerValueColor = palette.headerValueColor();
    }

    public int color(PlayClockColorSlot slot) {
        return switch (slot) {
            case HUD_LABEL -> hudLabelColor;
            case HUD_VALUE -> hudValueColor;
            case MARKER_LABEL -> markerLabelColor;
            case MARKER_VALUE -> markerValueColor;
            case TOOLTIP_TITLE -> tooltipTitleColor;
            case TOOLTIP_LABEL -> tooltipLabelColor;
            case TOOLTIP_VALUE -> tooltipValueColor;
            case HEADER_LABEL -> headerLabelColor;
            case HEADER_VALUE -> headerValueColor;
        };
    }

    public void setColor(PlayClockColorSlot slot, int color) {
        switch (slot) {
            case HUD_LABEL -> hudLabelColor = color;
            case HUD_VALUE -> hudValueColor = color;
            case MARKER_LABEL -> markerLabelColor = color;
            case MARKER_VALUE -> markerValueColor = color;
            case TOOLTIP_TITLE -> tooltipTitleColor = color;
            case TOOLTIP_LABEL -> tooltipLabelColor = color;
            case TOOLTIP_VALUE -> tooltipValueColor = color;
            case HEADER_LABEL -> headerLabelColor = color;
            case HEADER_VALUE -> headerValueColor = color;
        }
    }
}
