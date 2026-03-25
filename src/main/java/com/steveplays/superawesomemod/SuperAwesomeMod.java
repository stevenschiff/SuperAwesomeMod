package com.steveplays.superawesomemod;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SuperAwesomeMod implements ModInitializer {

    public static final String MOD_ID = "superawesomemod";

    // SLF4J logger — use this everywhere instead of System.out.println
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[SuperAwesomeMod] Initializing — MC 1.21.11 / Fabric Loader");

        // Register all mod content here, or delegate to registration classes:
        ModEvents.register();
    }
}
