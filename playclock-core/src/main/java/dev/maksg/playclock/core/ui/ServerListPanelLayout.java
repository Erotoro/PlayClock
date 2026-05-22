package dev.maksg.playclock.core.ui;

public record ServerListPanelLayout(int x, int y, int width, int height) {
    private static final int SCREEN_MARGIN = 20;
    private static final int RAIL_GAP_FROM_LIST = 28;
    private static final int RAIL_GAP_BETWEEN_CARDS = 12;
    private static final int RAIL_TOP_FALLBACK = 80;
    private static final int RAIL_MIN_X = 840;
    private static final int COMPACT_THRESHOLD = 940;
    private static final int COMPACT_TOP = 24;
    private static final int COMPACT_FOOTER_RESERVED_HEIGHT = 82;
    private static final int MARKER_GAP_FROM_LIST = 10;
    private static final int MARKER_GAP_BEFORE_RAIL = 8;

    public static ServerListPanelLayout summaryPanel(
            int screenWidth,
            int screenHeight,
            int listRight,
            int listTop,
            int width,
            int height) {
        if (compactMode(screenWidth)) {
            return new ServerListPanelLayout(
                    Math.max(SCREEN_MARGIN, screenWidth - width - SCREEN_MARGIN),
                    COMPACT_TOP,
                    width,
                    height);
        }

        return new ServerListPanelLayout(
                railX(screenWidth, listRight, width),
                railTop(listTop),
                width,
                height);
    }

    public static ServerListPanelLayout detailPanel(
            int screenWidth,
            int screenHeight,
            int listRight,
            int listTop,
            int summaryWidth,
            int summaryHeight,
            int detailWidth,
            int detailHeight) {
        if (compactMode(screenWidth)) {
            int x = Math.max(SCREEN_MARGIN, screenWidth - detailWidth - SCREEN_MARGIN);
            int y = clamp(
                    screenHeight - COMPACT_FOOTER_RESERVED_HEIGHT - detailHeight,
                    COMPACT_TOP + summaryHeight + RAIL_GAP_BETWEEN_CARDS,
                    Math.max(COMPACT_TOP + summaryHeight + RAIL_GAP_BETWEEN_CARDS, screenHeight - detailHeight - SCREEN_MARGIN));
            return new ServerListPanelLayout(x, y, detailWidth, detailHeight);
        }

        ServerListPanelLayout summary = summaryPanel(screenWidth, screenHeight, listRight, listTop, summaryWidth, summaryHeight);
        return new ServerListPanelLayout(
                railX(screenWidth, listRight, detailWidth),
                summary.y() + summary.height() + RAIL_GAP_BETWEEN_CARDS,
                detailWidth,
                detailHeight);
    }

    public static int markerX(int screenWidth, int listRight, int railX, int markerWidth) {
        if (compactMode(screenWidth)) {
            return -1;
        }

        int preferred = listRight + MARKER_GAP_FROM_LIST;
        int latestAllowed = railX - markerWidth - MARKER_GAP_BEFORE_RAIL;
        return latestAllowed >= preferred ? preferred : -1;
    }

    public static boolean compactMode(int screenWidth) {
        return screenWidth < COMPACT_THRESHOLD;
    }

    public static int screenMargin() {
        return SCREEN_MARGIN;
    }

    private static int railX(int screenWidth, int listRight, int width) {
        int preferred = listRight + RAIL_GAP_FROM_LIST;
        int minX = Math.max((screenWidth / 2) + 120, RAIL_MIN_X);
        int maxX = screenWidth - width - SCREEN_MARGIN;
        return clamp(preferred, minX, maxX);
    }

    private static int railTop(int listTop) {
        return Math.max(RAIL_TOP_FALLBACK, listTop);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
