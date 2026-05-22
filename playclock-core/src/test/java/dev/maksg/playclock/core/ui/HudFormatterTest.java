package dev.maksg.playclock.core.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import dev.maksg.playclock.core.config.PlayClockConfig;
import dev.maksg.playclock.core.model.SourceType;
import dev.maksg.playclock.core.model.TrackedTarget;
import dev.maksg.playclock.core.stats.PlaytimeStats;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class HudFormatterTest {

    @Test
    void createsCompactHudSnapshotWithOptionalMetrics() {
        HudSnapshot snapshot = HudFormatter.create(
                target(),
                stats(),
                PlayClockConfig.defaults(),
                "en_us",
                ZoneId.systemDefault());

        assertEquals("compact", snapshot.variant());
        assertEquals("ReallyWorld", snapshot.title());
        assertEquals(3, snapshot.metrics().size());
        assertEquals("playclock.label.total", snapshot.metrics().get(0).labelKey());
        assertEquals("2h 2m", snapshot.metrics().get(0).value());
        assertEquals("playclock.label.today", snapshot.metrics().get(1).labelKey());
        assertEquals("2m", snapshot.metrics().get(1).value());
        assertEquals("playclock.label.session", snapshot.metrics().get(2).labelKey());
        assertEquals("04:00", snapshot.metrics().get(2).value());
    }

    @Test
    void createsMinimalHudSnapshotWithOnlyTotalMetric() {
        PlayClockConfig config = new PlayClockConfig(
                true, true, true, "compact", "auto", "top_left",
                "minimal", true, true, true, "vanilla", "preset", "vanilla",
                0, 0, 0, 0, 0, 0, 0, 0, 0);

        HudSnapshot snapshot = HudFormatter.create(target(), stats(), config, "en_us", ZoneId.systemDefault());

        assertEquals("minimal", snapshot.variant());
        assertEquals(1, snapshot.metrics().size());
        assertEquals("playclock.label.total", snapshot.metrics().get(0).labelKey());
    }

    @Test
    void removesDisabledTodayAndSessionMetrics() {
        PlayClockConfig config = new PlayClockConfig(
                true, true, true, "compact", "auto", "top_left",
                "stacked", false, false, true, "vanilla", "preset", "vanilla",
                0, 0, 0, 0, 0, 0, 0, 0, 0);

        HudSnapshot snapshot = HudFormatter.create(target(), stats(), config, "en_us", ZoneId.systemDefault());

        assertEquals("stacked", snapshot.variant());
        assertEquals(1, snapshot.metrics().size());
        assertEquals("playclock.label.total", snapshot.metrics().get(0).labelKey());
    }

    @Test
    void returnsNullWhenStatsMissing() {
        assertNull(HudFormatter.create(target(), null, PlayClockConfig.defaults(), "en_us", ZoneId.systemDefault()));
    }

    private static TrackedTarget target() {
        return new TrackedTarget("server:a", "ReallyWorld", "reallyworld", SourceType.SAVED, true, false);
    }

    private static PlaytimeStats stats() {
        return new PlaytimeStats(
                7320,
                0,
                LocalDate.now(ZoneId.systemDefault()),
                120,
                240,
                Instant.parse("2026-05-21T14:05:00Z"));
    }
}
