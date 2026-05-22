package dev.maksg.playclock.core.time;

import java.time.Instant;
import java.time.ZoneId;

public interface Clock {

    Instant now();

    ZoneId zoneId();
}
