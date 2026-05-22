package dev.maksg.playclock.core.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ServerListPanelLayoutTest {

    @Test
    void placesWideSummaryInInspectorRailRelativeToListBounds() {
        assertEquals(
                new ServerListPanelLayout(840, 84, 176, 34),
                ServerListPanelLayout.summaryPanel(1400, 900, 760, 84, 176, 34));
    }

    @Test
    void stacksWideDetailUnderSummaryInSameRail() {
        assertEquals(
                new ServerListPanelLayout(840, 130, 212, 92),
                ServerListPanelLayout.detailPanel(1400, 900, 760, 84, 176, 34, 212, 92));
    }

    @Test
    void hidesWideMarkerWhenThereIsNoDedicatedLane() {
        assertEquals(-1, ServerListPanelLayout.markerX(1200, 860, 930, 90));
    }

    @Test
    void placesWideMarkerInsideDedicatedLane() {
        assertEquals(770, ServerListPanelLayout.markerX(1400, 760, 900, 90));
    }

    @Test
    void keepsCompactSummaryPinnedToTopRight() {
        assertEquals(
                new ServerListPanelLayout(636, 24, 144, 28),
                ServerListPanelLayout.summaryPanel(800, 480, 520, 84, 144, 28));
    }

    @Test
    void keepsCompactDetailAboveFooterArea() {
        assertEquals(
                new ServerListPanelLayout(616, 340, 164, 58),
                ServerListPanelLayout.detailPanel(800, 480, 520, 84, 144, 28, 164, 58));
    }

    @Test
    void hidesMarkersInCompactMode() {
        assertEquals(-1, ServerListPanelLayout.markerX(800, 520, 656, 44));
    }
}
