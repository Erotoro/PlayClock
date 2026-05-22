package dev.maksg.playclock.core.config;

import java.awt.Color;

public final class PlayClockColorCodec {

    private PlayClockColorCodec() {
    }

    public static int fromRgb(int red, int green, int blue) {
        return 0xFF000000
                | ((clamp(red) & 0xFF) << 16)
                | ((clamp(green) & 0xFF) << 8)
                | (clamp(blue) & 0xFF);
    }

    public static int red(int color) {
        return (color >> 16) & 0xFF;
    }

    public static int green(int color) {
        return (color >> 8) & 0xFF;
    }

    public static int blue(int color) {
        return color & 0xFF;
    }

    public static String toHexRgb(int color) {
        return "#%02X%02X%02X".formatted(red(color), green(color), blue(color));
    }

    public static int parseHexRgb(String value, int fallbackColor) {
        if (value == null) {
            return fallbackColor;
        }

        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return fallbackColor;
        }

        if (normalized.startsWith("#")) {
            normalized = normalized.substring(1);
        }

        if (normalized.length() != 6 || !normalized.matches("[0-9a-fA-F]{6}")) {
            return fallbackColor;
        }

        int rgb = Integer.parseInt(normalized, 16);
        return 0xFF000000 | rgb;
    }

    public static int parseRgbChannel(String value, int fallbackChannel) {
        if (value == null || value.isBlank()) {
            return clamp(fallbackChannel);
        }

        try {
            return clamp(Integer.parseInt(value.trim()));
        } catch (NumberFormatException ignored) {
            return clamp(fallbackChannel);
        }
    }

    public static float[] toHsv(int color) {
        return Color.RGBtoHSB(red(color), green(color), blue(color), null);
    }

    public static int fromHsv(float hue, float saturation, float value) {
        float normalizedHue = hue;
        while (normalizedHue < 0.0f) {
            normalizedHue += 1.0f;
        }
        while (normalizedHue > 1.0f) {
            normalizedHue -= 1.0f;
        }

        int rgb = Color.HSBtoRGB(
                clampUnit(normalizedHue),
                clampUnit(saturation),
                clampUnit(value));
        return 0xFF000000 | (rgb & 0x00FFFFFF);
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private static float clampUnit(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }
}
