package com.steveplays.superawesomemod;

import com.steveplays.superawesomemod.FreeLookData;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.CameraType;

public class SuperAwesomeModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        SuperAwesomeMod.LOGGER.info("[SuperAwesomeMod] Client initialized!");

        ModKeybindings.register();

        // Track perspective so we can reset free look when the player switches views.
        final CameraType[] lastCameraType = { CameraType.FIRST_PERSON };

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Open menu on keybind press.
            while (ModKeybindings.openMenu.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(new ModMenuScreen());
                }
            }

            // Maintain mod flight client-side every tick.
            // The vanilla server sends mayfly=false once at join for survival
            // players. Re-setting mayfly=true here overrides that every tick so
            // the client always permits flight — regardless of whether the server
            // has the mod installed.
            if (client.player != null && PlayerFlyData.isEnabled(client.player.getUUID())) {
                client.player.getAbilities().mayfly = true;
            }

            // Reset free look when the player switches camera perspective (F5).
            CameraType currentCameraType = client.options.getCameraType();
            if (currentCameraType != lastCameraType[0]) {
                lastCameraType[0] = currentCameraType;
                FreeLookData.setActive(false);
                FreeLookData.reset();
            }

            // Free look: hold mode vs toggle mode.
            if (FreeLookData.isToggleMode()) {
                while (ModKeybindings.freeLook.consumeClick()) {
                    boolean nowActive = !FreeLookData.isActive();
                    FreeLookData.setActive(nowActive);
                    if (!nowActive) FreeLookData.reset();
                }
            } else {
                boolean held = ModKeybindings.freeLook.isDown();
                FreeLookData.setActive(held);
                if (!held) FreeLookData.reset();
            }
        });
    }
}
