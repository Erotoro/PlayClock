package dev.maksg.playclock.core.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HudLayoutTest {

    @Test
    void computesExpectedAnchoredPositions() {
        assertEquals(new HudLayout(8, 8), HudLayout.anchored(320, 240, 154, 36, "top_left"));
        assertEquals(new HudLayout(158, 8), HudLayout.anchored(320, 240, 154, 36, "top_right"));
        assertEquals(new HudLayout(8, 196), HudLayout.anchored(320, 240, 116, 36, "bottom_left"));
        assertEquals(new HudLayout(158, 196), HudLayout.anchored(320, 240, 154, 36, "bottom_right"));
    }

    @Test
    void clampsHudInsideVerySmallScreens() {
        assertEquals(new HudLayout(8, 8), HudLayout.anchored(120, 80, 154, 46, "top_right"));
        assertEquals(new HudLayout(8, 26), HudLayout.anchored(120, 80, 154, 46, "bottom_right"));
    }
}
