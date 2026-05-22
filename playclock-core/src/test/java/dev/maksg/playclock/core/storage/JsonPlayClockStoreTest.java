package dev.maksg.playclock.core.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.maksg.playclock.core.config.PlayClockConfig;
import dev.maksg.playclock.core.model.SourceType;
import dev.maksg.playclock.core.model.TrackedTarget;
import dev.maksg.playclock.core.runtime.PlayClockState;
import dev.maksg.playclock.core.stats.PlaytimeStats;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JsonPlayClockStoreTest {

    @TempDir
    Path tempDir;

    @Test
    void savesAndLoadsFullStateSnapshot() throws Exception {
        Path file = tempDir.resolve("playclock.json");
        JsonPlayClockStore store = new JsonPlayClockStore(file);

        TrackedTarget server = new TrackedTarget(
                "multiplayer:mc.example.com",
                "mc.example.com",
                "mc.example.com",
                SourceType.SAVED,
                true,
                false);

        PlaytimeStats stats = new PlaytimeStats(
                3600,
                1200,
                LocalDate.of(2026, 5, 19),
                2400,
                300,
                Instant.parse("2026-05-19T11:22:33Z"));

        PlayClockState state = new PlayClockState(
                JsonPlayClockStore.CURRENT_SCHEMA_VERSION,
                PlayClockConfig.defaults(),
                Map.of(server.key(), server),
                Map.of(server.key(), stats));

        store.save(state);
        PlayClockState loaded = store.load();

        assertEquals(JsonPlayClockStore.CURRENT_SCHEMA_VERSION, loaded.schemaVersion());
        assertEquals(state.config(), loaded.config());
        assertEquals(server, loaded.targets().get(server.key()));
        assertEquals(stats, loaded.stats().get(server.key()));
    }

    @Test
    void returnsDefaultStateWhenFileDoesNotExist() throws Exception {
        JsonPlayClockStore store = new JsonPlayClockStore(tempDir.resolve("missing.json"));

        PlayClockState state = store.load();

        assertEquals(JsonPlayClockStore.CURRENT_SCHEMA_VERSION, state.schemaVersion());
        assertEquals(PlayClockConfig.defaults(), state.config());
        assertTrue(state.targets().isEmpty());
        assertTrue(state.stats().isEmpty());
    }

    @Test
    void persistsConfigChanges() throws Exception {
        Path file = tempDir.resolve("playclock-config.json");
        JsonPlayClockStore store = new JsonPlayClockStore(file);

        PlayClockConfig config = new PlayClockConfig(false, false, true, "compact", "ru_ru", "bottom_right");
        PlayClockState state = new PlayClockState(
                JsonPlayClockStore.CURRENT_SCHEMA_VERSION,
                config,
                Map.of(),
                Map.of());

        store.save(state);

        String rawJson = Files.readString(file);
        assertTrue(rawJson.contains("\"schemaVersion\":4"));
        assertTrue(rawJson.contains("\"preferredLanguage\":\"ru_ru\""));
        assertTrue(rawJson.contains("\"hudAnchor\":\"bottom_right\""));

        PlayClockState loaded = store.load();
        assertFalse(loaded.config().hudEnabled());
        assertFalse(loaded.config().badgeEnabled());
        assertTrue(loaded.config().tooltipsEnabled());
        assertEquals("compact", loaded.config().timeFormat());
        assertEquals("ru_ru", loaded.config().preferredLanguage());
        assertEquals("bottom_right", loaded.config().hudAnchor());
        assertEquals("compact", loaded.config().hudVariant());
        assertEquals("vanilla", loaded.config().colorPreset());
    }

    @Test
    void migratesOlderConfigSchemaAndKeepsBadgesEnabledByDefault() throws Exception {
        Path file = tempDir.resolve("playclock-v1.json");
        Files.writeString(file, """
                {
                  "schemaVersion": 1,
                  "config": {
                    "hudEnabled": true,
                    "tooltipsEnabled": true,
                    "timeFormat": "compact",
                    "preferredLanguage": "auto"
                  },
                  "targets": {},
                  "stats": {}
                }
                """);

        JsonPlayClockStore store = new JsonPlayClockStore(file);
        PlayClockState loaded = store.load();

        assertEquals(JsonPlayClockStore.CURRENT_SCHEMA_VERSION, loaded.schemaVersion());
        assertTrue(loaded.config().badgeEnabled());
        assertTrue(loaded.config().hudEnabled());
        assertTrue(loaded.config().tooltipsEnabled());
        assertEquals("top_left", loaded.config().hudAnchor());
        assertEquals("compact", loaded.config().hudVariant());
        assertEquals("preset", loaded.config().colorMode());
    }
}
