package com.steveplays.superawesomemod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.CameraType;

public class SuperAwesomeModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        SuperAwesomeMod.LOGGER.info("[SuperAwesomeMod] Client initialized!");

        ModKeybindings.register();
        ArmorHudOverlay.register();
        CombatHitboxRenderer.register();
        PvpDetectorOverlay.register();
        XrayLineRenderType.touch();

        final CameraType[] lastCameraType = { CameraType.FIRST_PERSON };

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Open menu on keybind press.
            while (ModKeybindings.openMenu.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(new ModMenuScreen());
                }
            }

            // Zoom: hold-to-zoom. Drain queued click events so they don't pile up,
            // then sample the live held state — only active in-game (no screen open).
            while (ModKeybindings.zoom.consumeClick()) { /* drain */ }
            ZoomData.setKeyHeld(client.screen == null && ModKeybindings.zoom.isDown());
            ZoomData.tick();

            // Maintain mod flight client-side every tick.
            if (client.player != null && PlayerFlyData.isEnabled(client.player.getUUID())) {
                client.player.getAbilities().mayfly = true;
            }

            // Free look: active only when enabled AND in third-person.
            // Reset offsets whenever the player switches perspective (F5).
            CameraType currentCamera = client.options.getCameraType();
            if (currentCamera != lastCameraType[0]) {
                lastCameraType[0] = currentCamera;
                FreeLookData.reset();
            }
            boolean thirdPerson = currentCamera != CameraType.FIRST_PERSON;
            FreeLookData.setActive(FreeLookData.isEnabled() && thirdPerson);
        });
    }
}
