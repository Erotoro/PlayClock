package dev.maksg.playclock.core.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TooltipLayoutTest {

    @Test
    void keepsTooltipInsideScreenBounds() {
        assertEquals(new TooltipLayout(194, 100), TooltipLayout.anchoredBelow(320, 240, 260, 100, 120, 48));
        assertEquals(new TooltipLayout(180, 154), TooltipLayout.anchoredBelow(320, 240, 180, 210, 100, 48));
        assertEquals(new TooltipLayout(6, 6), TooltipLayout.anchoredBelow(320, 240, -20, -10, 80, 40));
    }
}
