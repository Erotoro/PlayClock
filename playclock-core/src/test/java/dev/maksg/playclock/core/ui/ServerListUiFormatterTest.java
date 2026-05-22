package dev.maksg.playclock.core.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.maksg.playclock.core.config.PlayClockConfig;
import dev.maksg.playclock.core.model.SourceType;
import dev.maksg.playclock.core.model.TrackedTarget;
import dev.maksg.playclock.core.runtime.PlayClockState;
import dev.maksg.playclock.core.stats.PlaytimeStats;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ServerListUiFormatterTest {

    @Test
    void aggregatesOnlyMultiplayerTargetsForSummary() {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        TrackedTarget savedServer = new TrackedTarget("server:a", "Alpha", "alpha", SourceType.SAVED, true, false);
        TrackedTarget lanServer = new TrackedTarget("server:b", "Beta", "beta", SourceType.LAN, true, true);
        TrackedTarget world = new TrackedTarget("world:a", "World", "world", null, false, false);
        PlayClockState state = new PlayClockState(
                3,
                PlayClockConfig.defaults(),
                Map.of(savedServer.key(), savedServer, lanServer.key(), lanServer, world.key(), world),
                Map.of(
                        savedServer.key(), stats(today, 7200, 1800, 300, Instant.parse("2026-05-21T10:30:00Z")),
                        lanServer.key(), stats(today, 3600, 600, 60, Instant.parse("2026-05-21T11:30:00Z")),
                        world.key(), stats(today, 5400, 900, 120, Instant.parse("2026-05-21T09:30:00Z"))));

        ServerListSummarySnapshot summary = ServerListUiFormatter.createMultiplayerSummary(state, ZoneId.of("UTC"), "en_us");

        assertEquals("3h 0m", summary.totalText());
    }

    @Test
    void aggregatesOnlySingleplayerTargetsForWorldSummary() {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        TrackedTarget savedServer = new TrackedTarget("server:a", "Alpha", "alpha", SourceType.SAVED, true, false);
        TrackedTarget lanServer = new TrackedTarget("server:b", "Beta", "beta", SourceType.LAN, true, true);
        TrackedTarget world = new TrackedTarget("world:a", "World", "world", null, false, false);
        PlayClockState state = new PlayClockState(
                3,
                PlayClockConfig.defaults(),
                Map.of(savedServer.key(), savedServer, lanServer.key(), lanServer, world.key(), world),
                Map.of(
                        savedServer.key(), stats(today, 7200, 1800, 300, Instant.parse("2026-05-21T10:30:00Z")),
                        lanServer.key(), stats(today, 3600, 600, 60, Instant.parse("2026-05-21T11:30:00Z")),
                        world.key(), stats(today, 5400, 900, 120, Instant.parse("2026-05-21T09:30:00Z"))));

        ServerListSummarySnapshot summary = ServerListUiFormatter.createSingleplayerSummary(state, ZoneId.of("UTC"), "en_us");

        assertEquals("1h 30m", summary.totalText());
    }

    @Test
    void createsDailyBadgeAndDetailSnapshot() {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        Instant lastPlayedAt = LocalDateTime.of(today.getYear(), today.getMonth(), today.getDayOfMonth(), 14, 5)
                .atZone(ZoneId.of("UTC"))
                .toInstant();
        PlayClockConfig config = PlayClockConfig.defaults();
        TrackedTarget target = new TrackedTarget("server:a", "ReallyWorld", "reallyworld", SourceType.SAVED, true, false);
        PlaytimeStats stats = stats(today, 7320, 120, 240, lastPlayedAt);

        ServerListDailyBadgeSnapshot badge = ServerListUiFormatter.createDailyBadge(stats, config, "en_us");
        ServerListDetailSnapshot detail = ServerListUiFormatter.createDetail(target, stats, ZoneId.systemDefault(), config, "en_us");

        assertEquals("2m", badge.todayText());
        assertEquals("ReallyWorld", detail.titleText());
        assertEquals("2m", detail.todayText());
        assertEquals("2h 2m", detail.totalText());
        assertEquals("04:00", detail.sessionText());
        assertTrue(detail.lastPlayedText().startsWith("Today "));
    }

    @Test
    void hidesDailyBadgeWhenTodayTimeIsZero() {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        assertNull(ServerListUiFormatter.createDailyBadge(
                stats(today, 7320, 0, 240, Instant.parse("2026-05-21T14:05:00Z")),
                PlayClockConfig.defaults(),
                "en_us"));
    }

    @Test
    void hidesStaleDailyBadgeAndShowsZeroTodayWhenStatsAreFromPreviousDay() {
        PlayClockConfig config = PlayClockConfig.defaults();
        TrackedTarget target = new TrackedTarget("world:a", "New World", "new-world", null, false, false);
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        PlaytimeStats staleStats = new PlaytimeStats(
                60,
                0,
                today.minusDays(1),
                60,
                81,
                LocalDateTime.of(today.minusDays(1).getYear(), today.minusDays(1).getMonth(), today.minusDays(1).getDayOfMonth(), 11, 7)
                        .atZone(ZoneId.of("UTC"))
                        .toInstant());

        ServerListDailyBadgeSnapshot badge = ServerListUiFormatter.createDailyBadge(staleStats, config, "en_us");
        ServerListDetailSnapshot detail = ServerListUiFormatter.createDetail(target, staleStats, ZoneId.systemDefault(), config, "en_us");

        assertNull(badge);
        assertEquals("0s", detail.todayText());
        assertEquals("1m", detail.totalText());
        assertTrue(detail.lastPlayedText().startsWith("Yesterday "));
    }

    @Test
    void returnsNullSnapshotsWhenStatsMissing() {
        assertNull(ServerListUiFormatter.createDailyBadge(null, PlayClockConfig.defaults(), "en_us"));
        assertNull(ServerListUiFormatter.createDetail(
                new TrackedTarget("server:a", "Alpha", "alpha", SourceType.SAVED, true, false),
                null,
                ZoneId.of("UTC"),
                PlayClockConfig.defaults(),
                "en_us"));
    }

    private static PlaytimeStats stats(LocalDate todayDate, long totalSeconds, long todaySeconds, long sessionSeconds, Instant lastPlayedAt) {
        return new PlaytimeStats(
                totalSeconds,
                0,
                todayDate,
                todaySeconds,
                sessionSeconds,
                lastPlayedAt);
    }
}
