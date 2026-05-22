package dev.maksg.playclock.core.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.maksg.playclock.core.config.PlayClockConfig;
import dev.maksg.playclock.core.model.SourceType;
import dev.maksg.playclock.core.model.TrackedTarget;
import dev.maksg.playclock.core.storage.PlayClockStore;
import dev.maksg.playclock.core.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PlayClockRuntimeServiceTest {

    @Test
    void registersTargetTracksTimeAndPersistsSnapshot() throws Exception {
        InMemoryStore store = new InMemoryStore(new PlayClockState(1, PlayClockConfig.defaults(), Map.of(), Map.of()));
        MutableClock clock = new MutableClock(Instant.parse("2026-05-19T12:00:00Z"), ZoneId.of("UTC"));
        PlayClockRuntimeService service = new PlayClockRuntimeService(store, clock);

        TrackedTarget target = new TrackedTarget(
                "multiplayer:mc.example.com",
                "mc.example.com",
                "mc.example.com",
                SourceType.SAVED,
                true,
                false);

        service.registerTarget(target);
        service.startSession(target);
        assertSame(target, service.activeTarget());
        clock.advanceSeconds(75);
        service.flush();
        service.stopSession();

        PlayClockState snapshot = service.snapshot();
        assertSame(target, snapshot.targets().get(target.key()));
        assertEquals(75, snapshot.stats().get(target.key()).totalPlaytimeSeconds());
        assertEquals(75, snapshot.stats().get(target.key()).todayPlaytimeSeconds());
        assertNotNull(store.lastSavedState);
        assertEquals(75, store.lastSavedState.stats().get(target.key()).totalPlaytimeSeconds());
        assertNull(service.activeTarget());
    }

    @Test
    void loadsInitialStateFromStore() throws Exception {
        TrackedTarget target = new TrackedTarget(
                "singleplayer:my-world",
                "My World",
                "my-world",
                null,
                false,
                false);

        PlayClockState initialState = new PlayClockState(
                1,
                new PlayClockConfig(false, true, true, "short", "uk_ua", "bottom_left"),
                Map.of(target.key(), target),
                Map.of());

        PlayClockRuntimeService service = new PlayClockRuntimeService(
                new InMemoryStore(initialState),
                new MutableClock(Instant.parse("2026-05-19T09:00:00Z"), ZoneId.of("UTC")));

        assertEquals(initialState.config(), service.snapshot().config());
        assertSame(target, service.snapshot().targets().get(target.key()));
    }

    @Test
    void updatesConfigAndPersistsItImmediately() throws Exception {
        InMemoryStore store = new InMemoryStore(new PlayClockState(1, PlayClockConfig.defaults(), Map.of(), Map.of()));
        PlayClockRuntimeService service = new PlayClockRuntimeService(
                store,
                new MutableClock(Instant.parse("2026-05-19T09:00:00Z"), ZoneId.of("UTC")));

        PlayClockConfig updatedConfig = new PlayClockConfig(false, false, false, "clock", "ru_ru", "top_right");
        service.updateConfig(updatedConfig);

        assertEquals(updatedConfig, service.snapshot().config());
        assertNotNull(store.lastSavedState);
        assertEquals(updatedConfig, store.lastSavedState.config());
        assertTrue(store.saveCount > 0);
    }

    @Test
    void togglesHudAndPersistsUpdatedConfig() throws Exception {
        InMemoryStore store = new InMemoryStore(new PlayClockState(1, PlayClockConfig.defaults(), Map.of(), Map.of()));
        PlayClockRuntimeService service = new PlayClockRuntimeService(
                store,
                new MutableClock(Instant.parse("2026-05-19T09:00:00Z"), ZoneId.of("UTC")));

        boolean hudEnabled = service.toggleHud();

        assertEquals(false, hudEnabled);
        assertEquals(false, service.snapshot().config().hudEnabled());
        assertEquals(false, store.lastSavedState.config().hudEnabled());
    }

    @Test
    void togglesBadgesAndPersistsUpdatedConfig() throws Exception {
        InMemoryStore store = new InMemoryStore(new PlayClockState(1, PlayClockConfig.defaults(), Map.of(), Map.of()));
        PlayClockRuntimeService service = new PlayClockRuntimeService(
                store,
                new MutableClock(Instant.parse("2026-05-19T09:00:00Z"), ZoneId.of("UTC")));

        boolean badgesEnabled = service.toggleBadges();

        assertEquals(false, badgesEnabled);
        assertEquals(false, service.snapshot().config().badgeEnabled());
        assertEquals(false, store.lastSavedState.config().badgeEnabled());
    }

    private static final class InMemoryStore implements PlayClockStore {
        private final PlayClockState loadedState;
        private PlayClockState lastSavedState;
        private int saveCount;

        private InMemoryStore(PlayClockState loadedState) {
            this.loadedState = loadedState;
        }

        @Override
        public PlayClockState load() {
            return loadedState;
        }

        @Override
        public void save(PlayClockState state) {
            this.lastSavedState = state;
            this.saveCount++;
        }
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
