package com.steveplays.superawesomemod;

import net.fabricmc.api.ClientModInitializer;

public class SuperAwesomeModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Client-only initialization goes here (key bindings, rendering, HUD, etc.)
        SuperAwesomeMod.LOGGER.info("[SuperAwesomeMod] Client initialized!");
    }
}
