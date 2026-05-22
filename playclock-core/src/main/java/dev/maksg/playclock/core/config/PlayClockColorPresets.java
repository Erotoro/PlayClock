package dev.maksg.playclock.core.config;

public final class PlayClockColorPresets {
    private static final PlayClockColorPalette VANILLA = new PlayClockColorPalette(
            0xFFFFFFFF,
            0xFFFFD75E,
            0xFFFFFFFF,
            0xFFFFD75E,
            0xFFF0E5C8,
            0xFFB8B1A1,
            0xFFE0E0E0,
            0xFFFFFFFF,
            0xFFFFD75E);
    private static final PlayClockColorPalette GRASS = new PlayClockColorPalette(
            0xFFEAF6E5,
            0xFFB7E07A,
            0xFFEAF6E5,
            0xFFB7E07A,
            0xFFE4F0D8,
            0xFFB5C8A3,
            0xFFE7F0DF,
            0xFFEAF6E5,
            0xFFB7E07A);
    private static final PlayClockColorPalette SUNSET = new PlayClockColorPalette(
            0xFFFFF2E0,
            0xFFFFB36A,
            0xFFFFF2E0,
            0xFFFFB36A,
            0xFFFFE4C4,
            0xFFD8B28C,
            0xFFFFF0DD,
            0xFFFFF2E0,
            0xFFFFB36A);

    private PlayClockColorPresets() {
    }

    public static PlayClockColorPalette palette(String presetId) {
        return switch (presetId) {
            case "grass" -> GRASS;
            case "sunset" -> SUNSET;
            default -> VANILLA;
        };
    }
}
