package dev.maksg.playclock.core.runtime;

import dev.maksg.playclock.core.storage.JsonPlayClockStore;
import dev.maksg.playclock.core.time.SystemClock;
import java.io.IOException;
import java.nio.file.Path;

public final class PlayClockRuntimeFactory {

    private PlayClockRuntimeFactory() {
    }

    public static PlayClockRuntimeService create(Path dataFile) throws IOException {
        return new PlayClockRuntimeService(new JsonPlayClockStore(dataFile), new SystemClock());
    }
}
