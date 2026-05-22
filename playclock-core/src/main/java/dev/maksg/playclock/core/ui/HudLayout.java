package dev.maksg.playclock.core.ui;

public record HudLayout(int x, int y) {

    public static HudLayout anchored(int screenWidth, int screenHeight, int boxWidth, int boxHeight, String anchor) {
        int margin = 8;

        HudLayout layout = switch (anchor) {
            case "top_right" -> new HudLayout(screenWidth - boxWidth - margin, margin);
            case "bottom_left" -> new HudLayout(margin, screenHeight - boxHeight - margin);
            case "bottom_right" -> new HudLayout(screenWidth - boxWidth - margin, screenHeight - boxHeight - margin);
            default -> new HudLayout(margin, margin);
        };

        return new HudLayout(
                clamp(layout.x, margin, Math.max(margin, screenWidth - boxWidth - margin)),
                clamp(layout.y, margin, Math.max(margin, screenHeight - boxHeight - margin)));
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
