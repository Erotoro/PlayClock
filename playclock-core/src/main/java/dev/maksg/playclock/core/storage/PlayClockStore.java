package dev.maksg.playclock.core.storage;

import dev.maksg.playclock.core.runtime.PlayClockState;
import java.io.IOException;

public interface PlayClockStore {

    PlayClockState load() throws IOException;

    void save(PlayClockState state) throws IOException;

    default void saveDurably(PlayClockState state) throws IOException {
        save(state);
    }
}
