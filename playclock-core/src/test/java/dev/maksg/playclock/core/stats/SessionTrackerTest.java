package dev.maksg.playclock.core.stats;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.maksg.playclock.core.model.SourceType;
import dev.maksg.playclock.core.model.TrackedTarget;
import dev.maksg.playclock.core.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class SessionTrackerTest {

    @Test
    void accumulatesCurrentSessionTodayAndTotalTime() {
        MutableClock clock = new MutableClock(Instant.parse("2026-05-19T10:00:00Z"), ZoneId.of("UTC"));
        SessionTracker tracker = new SessionTracker(clock);
        TrackedTarget target = new TrackedTarget(
                "multiplayer:mc.example.com",
                "mc.example.com",
                "mc.example.com",
                SourceType.SAVED,
                true,
                false);

        tracker.start(target);
        clock.advanceSeconds(90);
        tracker.flushActiveTime();

        PlaytimeStats stats = tracker.statsFor(target.key());
        assertEquals(90, stats.totalPlaytimeSeconds());
        assertEquals(90, stats.todayPlaytimeSeconds());
        assertEquals(90, stats.currentSessionSeconds());
        assertEquals(Instant.parse("2026-05-19T10:01:30Z"), stats.lastPlayedAt());
    }

    @Test
    void splitsTimeAcrossLocalDayBoundary() {
        MutableClock clock = new MutableClock(Instant.parse("2026-05-19T23:59:30Z"), ZoneId.of("UTC"));
        SessionTracker tracker = new SessionTracker(clock);
        TrackedTarget target = new TrackedTarget(
                "singleplayer:my-world",
                "My World",
                "my-world",
                null,
                false,
                false);

        tracker.start(target);
        clock.advanceSeconds(60);
        tracker.flushActiveTime();

        PlaytimeStats stats = tracker.statsFor(target.key());
        assertEquals(60, stats.totalPlaytimeSeconds());
        assertEquals(30, stats.previousDayPlaytimeSeconds());
        assertEquals(30, stats.todayPlaytimeSeconds());
        assertEquals(60, stats.currentSessionSeconds());
        assertEquals(Instant.parse("2026-05-20T00:00:30Z"), stats.lastPlayedAt());
    }

    private static final class MutableClock implements Clock {
        private Instant now;
        private final ZoneId zoneId;

        private MutableClock(Instant now, ZoneId zoneId) {
            this.now = now;
            this.zoneId = zoneId;
        }

        @Override
        public Instant now() {
            return now;
        }

        @Override
        public ZoneId zoneId() {
            return zoneId;
        }

        private void advanceSeconds(long seconds) {
            now = now.plusSeconds(seconds);
        }
    }
}
