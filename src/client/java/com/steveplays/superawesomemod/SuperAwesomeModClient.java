package com.steveplays.superawesomemod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class SuperAwesomeModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        SuperAwesomeMod.LOGGER.info("[SuperAwesomeMod] Client initialized!");

        ModKeybindings.register();

        // Check the keybind every tick; open the menu when it is pressed.
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (ModKeybindings.openMenu.consumeClick()) {
                if (client.screen == null) {  // don't stack screens
                    client.setScreen(new ModMenuScreen());
                }
            }
        });
    }
}
