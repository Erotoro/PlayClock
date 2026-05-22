package dev.maksg.playclock.mc261x;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PlayClock261x implements ModInitializer {
    public static final String MOD_ID = "playclock";
    public static final Logger LOGGER = LoggerFactory.getLogger("PlayClock/26.1.x");

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing PlayClock common entrypoint for Minecraft 26.1.x");
    }
}
