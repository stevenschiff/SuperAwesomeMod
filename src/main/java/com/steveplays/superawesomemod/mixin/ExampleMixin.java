package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.SuperAwesomeMod;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Demonstration mixin: logs a message when the server finishes loading a world.
 * Replace or remove this once you have real mixin targets.
 * Prefer Fabric API events over mixins whenever a suitable event exists.
 */
@Mixin(MinecraftServer.class)
public class ExampleMixin {

    @Inject(method = "loadLevel", at = @At("HEAD"))
    private void onLoadLevel(CallbackInfo ci) {
        SuperAwesomeMod.LOGGER.info("[SuperAwesomeMod] World is loading — mixin active!");
    }
}
