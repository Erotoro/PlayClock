package dev.maksg.playclock.mc261x.client;

import dev.maksg.playclock.core.model.TrackedTarget;

final class PlayClock261xWorldListOverlayState {
    private static TrackedTarget hoveredTarget;
    private static int hoveredMouseX;
    private static int hoveredMouseY;
    private static int listRight;
    private static int listTop;

    private PlayClock261xWorldListOverlayState() {
    }

    static void beginFrame() {
        hoveredTarget = null;
        hoveredMouseX = 0;
        hoveredMouseY = 0;
        listRight = 0;
        listTop = Integer.MAX_VALUE;
    }

    static void recordRowLayout(int rowX, int rowY, int rowWidth, int rowHeight) {
        listRight = Math.max(listRight, rowX + rowWidth);
        listTop = Math.min(listTop, rowY);
    }

    static void recordHoveredTarget(TrackedTarget target, int mouseX, int mouseY) {
        hoveredTarget = target;
        hoveredMouseX = mouseX;
        hoveredMouseY = mouseY;
    }

    static TrackedTarget hoveredTarget() {
        return hoveredTarget;
    }

    static int listRight() {
        return listRight;
    }

    static int listTop() {
        return listTop == Integer.MAX_VALUE ? 80 : listTop;
    }

    static int hoveredMouseX() {
        return hoveredMouseX;
    }

    static int hoveredMouseY() {
        return hoveredMouseY;
    }
}
