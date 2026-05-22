package dev.maksg.playclock.core.runtime;

import dev.maksg.playclock.core.config.PlayClockConfig;
import dev.maksg.playclock.core.model.TrackedTarget;
import dev.maksg.playclock.core.stats.PlaytimeStats;
import java.util.Map;

public record PlayClockState(
        int schemaVersion,
        PlayClockConfig config,
        Map<String, TrackedTarget> targets,
        Map<String, PlaytimeStats> stats) {
}
