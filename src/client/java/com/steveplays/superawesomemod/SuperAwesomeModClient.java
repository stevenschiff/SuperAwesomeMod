package com.steveplays.superawesomemod;

import com.steveplays.superawesomemod.mixin.MinecraftAutoclickerInvoker;
import com.steveplays.superawesomemod.mixin.OptionInstanceAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.CameraType;
import net.minecraft.client.Options;
import net.minecraft.tags.ItemTags;

public class SuperAwesomeModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        SuperAwesomeMod.LOGGER.info("[SuperAwesomeMod] Client initialized!");

        ModKeybindings.register();
        ArmorHudOverlay.register();
        CombatHitboxRenderer.register();
        CombatCrosshairOverlay.register();
        CombatPotionEffectsOverlay.register();
        PvpDetectorOverlay.register();
        AppleSkinOverlay.register();
        LODTerrainRenderer.register();
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

            // Freecam movement: WASD/Space/Shift translates the freecam position.
            // Snapshot prev BEFORE applying input so the camera mixin can lerp from
            // last tick's position across the frames between ticks.
            if (FreecamData.isEnabled() && client.screen == null && client.player != null) {
                FreecamData.beginTick();
                Options o = client.options;
                float forward = (o.keyUp.isDown()    ? 1f : 0f) - (o.keyDown.isDown()  ? 1f : 0f);
                float strafeR = (o.keyRight.isDown() ? 1f : 0f) - (o.keyLeft.isDown()  ? 1f : 0f);
                float vert    = (o.keyJump.isDown()  ? 1f : 0f) - (o.keyShift.isDown() ? 1f : 0f);
                float yawRad = (float) Math.toRadians(FreecamData.getYaw());
                double sinY = Math.sin(yawRad);
                double cosY = Math.cos(yawRad);
                double dx = -forward * sinY - strafeR * cosY;
                double dz =  forward * cosY - strafeR * sinY;
                double horiz = Math.sqrt(dx * dx + dz * dz);
                if (horiz > 1.0) { dx /= horiz; dz /= horiz; }
                float speed = FreecamData.getSpeed();
                FreecamData.smoothMove(dx * speed, vert * speed, dz * speed);
            }

            // Render distance override: force the client's render distance value
            // beyond vanilla limits using the accessor mixin.
            if (RenderDistanceData.isEnabled() && client.player != null) {
                int dist = RenderDistanceData.getDistance();
                boolean singleplayer = client.getSingleplayerServer() != null;
                int current = client.options.renderDistance().get();
                if (current != dist) {
                    @SuppressWarnings("unchecked")
                    OptionInstanceAccessor<Integer> accessor =
                        (OptionInstanceAccessor<Integer>)(Object) client.options.renderDistance();
                    accessor.superawesomemod$setValue(dist);
                    // Trigger chunk rebuild so the renderer uses the new distance.
                    client.levelRenderer.allChanged();
                    // Update integrated server view distance in singleplayer.
                    if (singleplayer) {
                        client.getSingleplayerServer().getPlayerList().setViewDistance(dist);
                    }
                }

                // LOD terrain: update player position for the generator.
                int pcx = (int) Math.floor(client.player.getX()) >> 4;
                int pcz = (int) Math.floor(client.player.getZ()) >> 4;
                LODChunkData.setPlayerPos(pcx, pcz);
                LODChunkData.setLodRadius(dist);

                // In singleplayer the server sends real chunks at the full
                // distance, so skip LOD rendering everywhere. In multiplayer
                // skip only the server's actual view distance (real chunks).
                if (singleplayer) {
                    LODChunkData.setSkipRadius(dist);
                } else {
                    LODChunkData.setSkipRadius(12);
                }

                // Auto-detect seed in singleplayer.
                if (!RenderDistanceData.isSeedSet() && singleplayer) {
                    long seed = client.getSingleplayerServer().overworld().getSeed();
                    RenderDistanceData.setSeed(seed);
                }

                // In multiplayer, start LOD generator if seed is available.
                // In singleplayer, LOD is not needed (real chunks cover full
                // distance) so don't start the generator.
                if (!singleplayer && RenderDistanceData.isSeedSet()
                        && !LODHeightmapGenerator.isRunning()
                        && client.level != null) {
                    LODHeightmapGenerator.start(
                        RenderDistanceData.getSeed(),
                        client.level.registryAccess()
                    );
                }
            } else if (!RenderDistanceData.isEnabled() && LODHeightmapGenerator.isRunning()) {
                LODHeightmapGenerator.stop();
            }

            // 1.7 sword blocking: track whether the player is holding a sword
            // with the use key held (custom blocking state for animation).
            if (client.player != null && OldPvpData.isBlockingEnabled()) {
                boolean holdsSword = client.player.getMainHandItem().is(ItemTags.SWORDS);
                boolean useKeyHeld = client.options.keyUse.isDown();
                boolean notActuallyUsing = !client.player.isUsingItem();
                OldPvpData.setCustomBlocking(holdsSword && useKeyHeld && notActuallyUsing);
            } else {
                OldPvpData.setCustomBlocking(false);
            }

            // Autoclicker: fire as many simulated clicks as elapsed wall-clock allows.
            if (AutoclickerData.isEnabled() && client.screen == null
                && client.player != null && client.level != null) {
                int n = AutoclickerData.clicksToFire(System.nanoTime());
                if (n > 0) {
                    MinecraftAutoclickerInvoker inv = (MinecraftAutoclickerInvoker)(Object) client;
                    if (AutoclickerData.getButton() == AutoclickerData.Button.LEFT) {
                        for (int i = 0; i < n; i++) inv.superawesomemod$startAttack();
                    } else {
                        for (int i = 0; i < n; i++) inv.superawesomemod$startUseItem();
                    }
                }
            }
        });
    }
}
