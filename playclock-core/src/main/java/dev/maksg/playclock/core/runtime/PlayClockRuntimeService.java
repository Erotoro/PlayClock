package dev.maksg.playclock.core.runtime;

import dev.maksg.playclock.core.storage.PlayClockStore;
import dev.maksg.playclock.core.model.TrackedTarget;
import dev.maksg.playclock.core.stats.PlaytimeStats;
import dev.maksg.playclock.core.stats.SessionTracker;
import dev.maksg.playclock.core.time.Clock;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public final class PlayClockRuntimeService {
    private static final Duration DURABLE_SAVE_INTERVAL = Duration.ofSeconds(10);

    private final PlayClockStore store;
    private final Clock clock;
    private final SessionTracker tracker;
    private PlayClockState state;
    private String activeTargetKey;
    private Instant lastDurableSaveAt;

    public PlayClockRuntimeService(PlayClockStore store, Clock clock) throws IOException {
        this.store = store;
        this.clock = clock;
        this.tracker = new SessionTracker(clock);
        this.state = sanitizeLoadedState(store.load());
        this.tracker.restoreStats(this.state.stats());
        this.lastDurableSaveAt = clock.now();
    }

    public void registerTarget(dev.maksg.playclock.core.model.TrackedTarget target) {
        Map<String, dev.maksg.playclock.core.model.TrackedTarget> targets = new LinkedHashMap<>(state.targets());
        targets.put(target.key(), target);
        state = new PlayClockState(state.schemaVersion(), state.config(), Map.copyOf(targets), state.stats());
    }

    public void startSession(dev.maksg.playclock.core.model.TrackedTarget target) {
        registerTarget(target);
        tracker.start(target);
        activeTargetKey = target.key();
    }

    public void flush() {
        tracker.flushActiveTime();
        refreshStatsFromTracker();
        persist();
        maybePersistDurably();
    }

    public void stopSession() {
        flush();
        activeTargetKey = null;
        persistDurably();
    }

    public void shutdown() {
        if (activeTargetKey != null) {
            tracker.flushActiveTime();
            refreshStatsFromTracker();
        }

        persistDurably();
    }

    public PlayClockState snapshot() {
        return state;
    }

    public void updateConfig(dev.maksg.playclock.core.config.PlayClockConfig config) {
        state = new PlayClockState(state.schemaVersion(), config, state.targets(), state.stats());
        persist();
    }

    public boolean toggleHud() {
        dev.maksg.playclock.core.config.PlayClockConfig current = state.config();
        boolean nextHudEnabled = !current.hudEnabled();
        updateConfig(current.withHudEnabled(nextHudEnabled));
        return nextHudEnabled;
    }

    public boolean toggleBadges() {
        dev.maksg.playclock.core.config.PlayClockConfig current = state.config();
        boolean nextBadgeEnabled = !current.badgeEnabled();
        updateConfig(current.withBadgeEnabled(nextBadgeEnabled));
        return nextBadgeEnabled;
    }

    public TrackedTarget activeTarget() {
        return activeTargetKey == null ? null : state.targets().get(activeTargetKey);
    }

    private void refreshStatsFromTracker() {
        if (activeTargetKey == null) {
            return;
        }

        Map<String, PlaytimeStats> stats = new LinkedHashMap<>(state.stats());
        stats.put(activeTargetKey, tracker.statsFor(activeTargetKey));
        state = new PlayClockState(state.schemaVersion(), state.config(), state.targets(), Map.copyOf(stats));
    }

    private void persist() {
        try {
            store.save(state);
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to persist PlayClock state", exception);
        }
    }

    private void maybePersistDurably() {
        Instant now = clock.now();
        if (now.isBefore(lastDurableSaveAt.plus(DURABLE_SAVE_INTERVAL))) {
            return;
        }

        persistDurably();
    }

    private void persistDurably() {
        try {
            store.saveDurably(state);
            lastDurableSaveAt = clock.now();
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to durably persist PlayClock state", exception);
        }
    }

    private static PlayClockState sanitizeLoadedState(PlayClockState loadedState) {
        Map<String, PlaytimeStats> sanitizedStats = new LinkedHashMap<>();
        loadedState.stats().forEach((key, stats) -> sanitizedStats.put(key, new PlaytimeStats(
                stats.totalPlaytimeSeconds(),
                stats.previousDayPlaytimeSeconds(),
                stats.todayDate(),
                stats.todayPlaytimeSeconds(),
                0,
                stats.lastPlayedAt())));

        return new PlayClockState(
                loadedState.schemaVersion(),
                loadedState.config(),
                loadedState.targets(),
                Map.copyOf(sanitizedStats));
    }
}
