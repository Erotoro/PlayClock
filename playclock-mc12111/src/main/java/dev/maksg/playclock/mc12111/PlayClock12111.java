package dev.maksg.playclock.mc12111;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PlayClock12111 implements ModInitializer {
    public static final String MOD_ID = "playclock";
    public static final Logger LOGGER = LoggerFactory.getLogger("PlayClock/1.21.11");

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing PlayClock common entrypoint for Minecraft 1.21.11");
    }
}
