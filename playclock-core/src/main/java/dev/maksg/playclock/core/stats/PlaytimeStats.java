package dev.maksg.playclock.core.stats;

import java.time.Instant;
import java.time.LocalDate;

public record PlaytimeStats(
        long totalPlaytimeSeconds,
        long previousDayPlaytimeSeconds,
        LocalDate todayDate,
        long todayPlaytimeSeconds,
        long currentSessionSeconds,
        Instant lastPlayedAt) {

    public PlaytimeStats withAccumulatedTime(
            LocalDate todayDate,
            long addedPreviousDaySeconds,
            long addedTodaySeconds,
            long addedSessionSeconds,
            Instant lastPlayedAt) {
        long nextPreviousDaySeconds = this.previousDayPlaytimeSeconds;
        long nextTodaySeconds = this.todayPlaytimeSeconds;

        if (!todayDate.equals(this.todayDate)) {
            nextPreviousDaySeconds = this.todayPlaytimeSeconds + addedPreviousDaySeconds;
            nextTodaySeconds = addedTodaySeconds;
        } else {
            nextPreviousDaySeconds += addedPreviousDaySeconds;
            nextTodaySeconds += addedTodaySeconds;
        }

        return new PlaytimeStats(
                totalPlaytimeSeconds + addedPreviousDaySeconds + addedTodaySeconds,
                nextPreviousDaySeconds,
                todayDate,
                nextTodaySeconds,
                currentSessionSeconds + addedSessionSeconds,
                lastPlayedAt);
    }

    public static PlaytimeStats empty(LocalDate todayDate) {
        return new PlaytimeStats(0, 0, todayDate, 0, 0, null);
    }
}
