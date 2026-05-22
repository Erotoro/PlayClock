package dev.maksg.playclock.core.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import dev.maksg.playclock.core.model.TrackedTarget;
import org.junit.jupiter.api.Test;

class WorldIdentityResolverTest {

    @Test
    void resolvesWorldIdentityFromFolderName() {
        TrackedTarget target = WorldIdentityResolver.resolve("My Survival", "Survival World", null);

        assertEquals("singleplayer:my-survival", target.key());
        assertEquals("Survival World", target.displayValue());
        assertEquals("my-survival", target.normalizedValue());
        assertFalse(target.multiplayer());
        assertNull(target.sourceType());
    }

    @Test
    void prefersStableExplicitIdentityWhenPresent() {
        TrackedTarget target = WorldIdentityResolver.resolve("World1", "World One", "level-uuid-123");

        assertEquals("singleplayer:level-uuid-123", target.key());
        assertEquals("World One", target.displayValue());
        assertEquals("level-uuid-123", target.normalizedValue());
    }
}
