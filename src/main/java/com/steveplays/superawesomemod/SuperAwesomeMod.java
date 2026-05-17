package com.steveplays.superawesomemod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SuperAwesomeMod implements ModInitializer {

    public static final String MOD_ID = "superawesomemod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[SuperAwesomeMod] Initializing — MC 1.21.11 / Fabric Loader");

        ModEvents.register();
        ModCommands.register();

        // Remove all fake players when the server stops so they aren't
        // persisted to disk or left in a broken state on world reload.
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> FakePlayerManager.removeAll());
    }
}
