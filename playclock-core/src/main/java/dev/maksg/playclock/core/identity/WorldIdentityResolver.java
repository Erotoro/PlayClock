package dev.maksg.playclock.core.identity;

import dev.maksg.playclock.core.model.TrackedTarget;
import java.util.Locale;

public final class WorldIdentityResolver {

    private WorldIdentityResolver() {
    }

    public static TrackedTarget resolve(String folderName, String displayName, String stableIdentity) {
        String normalizedValue = normalizeIdentity(stableIdentity == null || stableIdentity.isBlank() ? folderName : stableIdentity);
        return new TrackedTarget(
                "singleplayer:" + normalizedValue,
                displayName,
                normalizedValue,
                null,
                false,
                false);
    }

    private static String normalizeIdentity(String rawValue) {
        String trimmed = rawValue == null ? "" : rawValue.trim().toLowerCase(Locale.ROOT);
        return trimmed.replaceAll("[^a-z0-9]+", "-").replaceAll("(^-+|-+$)", "");
    }
}
