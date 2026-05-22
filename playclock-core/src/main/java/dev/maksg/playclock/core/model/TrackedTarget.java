package dev.maksg.playclock.core.model;

public record TrackedTarget(
        String key,
        String displayValue,
        String normalizedValue,
        SourceType sourceType,
        boolean multiplayer,
        boolean localAddress) {
}
