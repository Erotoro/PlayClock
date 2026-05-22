package dev.maksg.playclock.core.ui;

public record TooltipLayout(int x, int y) {

    public static TooltipLayout anchoredBelow(
            int screenWidth,
            int screenHeight,
            int preferredX,
            int preferredY,
            int estimatedWidth,
            int estimatedHeight) {
        int margin = 6;
        int x = clamp(preferredX, margin, Math.max(margin, screenWidth - estimatedWidth - margin));
        int y = preferredY;

        if (y + estimatedHeight > screenHeight - margin) {
            y = Math.max(margin, preferredY - estimatedHeight - 8);
        }

        y = clamp(y, margin, Math.max(margin, screenHeight - estimatedHeight - margin));
        return new TooltipLayout(x, y);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
