package dev.maksg.playclock.core.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.maksg.playclock.core.config.PlayClockConfig;
import dev.maksg.playclock.core.runtime.PlayClockState;
import dev.maksg.playclock.core.stats.PlaytimeStats;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public final class JsonPlayClockStore implements PlayClockStore {
    public static final int CURRENT_SCHEMA_VERSION = 4;

    private final Path file;
    private final Gson gson;

    public JsonPlayClockStore(Path file) {
        this.file = file;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Instant.class, new InstantJsonAdapter())
                .registerTypeAdapter(LocalDate.class, new LocalDateJsonAdapter())
                .create();
    }

    @Override
    public PlayClockState load() throws IOException {
        if (Files.notExists(file)) {
            return emptyState();
        }

        try (Reader reader = Files.newBufferedReader(file)) {
            SerializedState serialized = gson.fromJson(reader, SerializedState.class);
            if (serialized == null) {
                return emptyState();
            }

            PlayClockConfig config = serialized.config == null
                    ? PlayClockConfig.defaults()
                    : migrateConfig(serialized.config, serialized.schemaVersion);

            return new PlayClockState(
                    Math.max(serialized.schemaVersion, CURRENT_SCHEMA_VERSION),
                    config,
                    serialized.targets == null ? Map.of() : Map.copyOf(serialized.targets),
                    serialized.stats == null ? Map.of() : Map.copyOf(serialized.stats));
        }
    }

    @Override
    public void save(PlayClockState state) throws IOException {
        persist(state, false);
    }

    @Override
    public void saveDurably(PlayClockState state) throws IOException {
        persist(state, true);
    }

    private void persist(PlayClockState state, boolean durable) throws IOException {
        Files.createDirectories(file.getParent());
        Path tempFile = file.resolveSibling(file.getFileName() + ".tmp");

        SerializedState serialized = new SerializedState(
                state.schemaVersion(),
                state.config(),
                new LinkedHashMap<>(state.targets()),
                new LinkedHashMap<>(state.stats()));

        try (Writer writer = Files.newBufferedWriter(tempFile)) {
            gson.toJson(serialized, writer);
        }

        if (durable) {
            forceFile(tempFile);
        }

        Files.move(tempFile, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);

        if (durable) {
            forceFile(file);
        }
    }

    private static void forceFile(Path path) throws IOException {
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.WRITE)) {
            channel.force(true);
        }
    }

    private static PlayClockState emptyState() {
        return new PlayClockState(CURRENT_SCHEMA_VERSION, PlayClockConfig.defaults(), Map.of(), Map.of());
    }

    private static PlayClockConfig migrateConfig(PlayClockConfig config, int schemaVersion) {
        PlayClockConfig defaults = PlayClockConfig.defaults();
        if (schemaVersion < 2) {
            config = new PlayClockConfig(
                    config.hudEnabled(),
                    true,
                    config.tooltipsEnabled(),
                    config.timeFormat(),
                    config.preferredLanguage(),
                    "top_left",
                    defaults.hudVariant(),
                    defaults.showTodayInHud(),
                    defaults.showSessionInHud(),
                    defaults.showHeaderSummary(),
                    defaults.tooltipTheme(),
                    defaults.colorMode(),
                    defaults.colorPreset(),
                    defaults.hudLabelColor(),
                    defaults.hudValueColor(),
                    defaults.markerLabelColor(),
                    defaults.markerValueColor(),
                    defaults.tooltipTitleColor(),
                    defaults.tooltipLabelColor(),
                    defaults.tooltipValueColor(),
                    defaults.headerLabelColor(),
                    defaults.headerValueColor());
        }

        if (schemaVersion < 3) {
            config = new PlayClockConfig(
                    config.hudEnabled(),
                    config.badgeEnabled(),
                    config.tooltipsEnabled(),
                    config.timeFormat(),
                    config.preferredLanguage(),
                    "top_left",
                    defaults.hudVariant(),
                    defaults.showTodayInHud(),
                    defaults.showSessionInHud(),
                    defaults.showHeaderSummary(),
                    defaults.tooltipTheme(),
                    defaults.colorMode(),
                    defaults.colorPreset(),
                    defaults.hudLabelColor(),
                    defaults.hudValueColor(),
                    defaults.markerLabelColor(),
                    defaults.markerValueColor(),
                    defaults.tooltipTitleColor(),
                    defaults.tooltipLabelColor(),
                    defaults.tooltipValueColor(),
                    defaults.headerLabelColor(),
                    defaults.headerValueColor());
        }

        if (schemaVersion < 4) {
            return new PlayClockConfig(
                    config.hudEnabled(),
                    config.badgeEnabled(),
                    config.tooltipsEnabled(),
                    config.timeFormat(),
                    config.preferredLanguage(),
                    config.hudAnchor(),
                    defaults.hudVariant(),
                    defaults.showTodayInHud(),
                    defaults.showSessionInHud(),
                    defaults.showHeaderSummary(),
                    defaults.tooltipTheme(),
                    defaults.colorMode(),
                    defaults.colorPreset(),
                    defaults.hudLabelColor(),
                    defaults.hudValueColor(),
                    defaults.markerLabelColor(),
                    defaults.markerValueColor(),
                    defaults.tooltipTitleColor(),
                    defaults.tooltipLabelColor(),
                    defaults.tooltipValueColor(),
                    defaults.headerLabelColor(),
                    defaults.headerValueColor());
        }

        return config;
    }

    private record SerializedState(
            int schemaVersion,
            PlayClockConfig config,
            Map<String, dev.maksg.playclock.core.model.TrackedTarget> targets,
            Map<String, PlaytimeStats> stats) {
    }
}
