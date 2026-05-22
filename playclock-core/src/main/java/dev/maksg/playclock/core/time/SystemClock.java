package dev.maksg.playclock.core.time;

import java.time.Instant;
import java.time.ZoneId;

public final class SystemClock implements Clock {

    @Override
    public Instant now() {
        return Instant.now();
    }

    @Override
    public ZoneId zoneId() {
        return ZoneId.systemDefault();
    }
}
