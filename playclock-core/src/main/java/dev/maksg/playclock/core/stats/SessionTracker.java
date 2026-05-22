package dev.maksg.playclock.core.stats;

import dev.maksg.playclock.core.model.TrackedTarget;
import dev.maksg.playclock.core.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

public final class SessionTracker {
    private final Clock clock;
    private final Map<String, PlaytimeStats> statsByTarget = new HashMap<>();

    private TrackedTarget activeTarget;
    private Instant sessionStartedAt;
    private Instant lastFlushedAt;

    public SessionTracker(Clock clock) {
        this.clock = clock;
    }

    public void start(TrackedTarget target) {
        activeTarget = target;
        sessionStartedAt = clock.now();
        lastFlushedAt = sessionStartedAt;
    }

    public void flushActiveTime() {
        if (activeTarget == null) {
            return;
        }

        Instant now = clock.now();
        if (!now.isAfter(lastFlushedAt)) {
            return;
        }

        Allocation allocation = allocate(lastFlushedAt, now);
        LocalDate todayDate = ZonedDateTime.ofInstant(now, clock.zoneId()).toLocalDate();
        PlaytimeStats current = statsByTarget.getOrDefault(activeTarget.key(), PlaytimeStats.empty(todayDate));
        PlaytimeStats next = current.withAccumulatedTime(
                todayDate,
                allocation.previousDaySeconds(),
                allocation.todaySeconds(),
                allocation.previousDaySeconds() + allocation.todaySeconds(),
                now);

        statsByTarget.put(activeTarget.key(), next);
        lastFlushedAt = now;
    }

    public PlaytimeStats statsFor(String targetKey) {
        return statsByTarget.getOrDefault(targetKey, PlaytimeStats.empty(LocalDate.now(clock.zoneId())));
    }

    private Allocation allocate(Instant startedAt, Instant endedAt) {
        ZonedDateTime started = ZonedDateTime.ofInstant(startedAt, clock.zoneId());
        ZonedDateTime ended = ZonedDateTime.ofInstant(endedAt, clock.zoneId());

        if (started.toLocalDate().equals(ended.toLocalDate())) {
            return new Allocation(0, endedAt.getEpochSecond() - startedAt.getEpochSecond());
        }

        ZonedDateTime midnight = ended.toLocalDate().atStartOfDay(clock.zoneId());
        long previousDaySeconds = midnight.toEpochSecond() - startedAt.getEpochSecond();
        long todaySeconds = endedAt.getEpochSecond() - midnight.toEpochSecond();
        return new Allocation(previousDaySeconds, todaySeconds);
    }

    private record Allocation(long previousDaySeconds, long todaySeconds) {
    }
}
