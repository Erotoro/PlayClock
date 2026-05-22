package dev.maksg.playclock.core.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.maksg.playclock.core.model.SourceType;
import dev.maksg.playclock.core.model.TrackedTarget;
import org.junit.jupiter.api.Test;

class ServerIdentityNormalizerTest {

    @Test
    void normalizesDefaultPortAndHostCaseForSavedServer() {
        TrackedTarget target = ServerIdentityNormalizer.normalize("  MC.Example.Com:25565  ", SourceType.SAVED);

        assertEquals("multiplayer:mc.example.com", target.key());
        assertEquals("MC.Example.Com:25565", target.displayValue());
        assertEquals("mc.example.com", target.normalizedValue());
        assertEquals(SourceType.SAVED, target.sourceType());
        assertTrue(target.multiplayer());
    }

    @Test
    void preservesNonDefaultPortForLocalhost() {
        TrackedTarget target = ServerIdentityNormalizer.normalize("LOCALHOST:24454", SourceType.DIRECT);

        assertEquals("multiplayer:localhost:24454", target.key());
        assertEquals("LOCALHOST:24454", target.displayValue());
        assertEquals("localhost:24454", target.normalizedValue());
        assertEquals(SourceType.DIRECT, target.sourceType());
        assertTrue(target.localAddress());
    }

    @Test
    void keepsIpv6BracketFormatStable() {
        TrackedTarget target = ServerIdentityNormalizer.normalize("[2001:DB8::1]:25565", SourceType.LAN);

        assertEquals("multiplayer:[2001:db8::1]", target.key());
        assertEquals("[2001:DB8::1]:25565", target.displayValue());
        assertEquals("[2001:db8::1]", target.normalizedValue());
        assertEquals(SourceType.LAN, target.sourceType());
    }
}
