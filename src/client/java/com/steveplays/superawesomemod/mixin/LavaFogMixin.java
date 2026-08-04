package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.NoFogData;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.LavaFogEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Removes lava fog by pushing fog distances very far when No Fog is enabled.
 */
@Mixin(LavaFogEnvironment.class)
public class LavaFogMixin {

    @Inject(method = "setupFog", at = @At("TAIL"))
    private void superawesomemod$clearLavaFog(FogData fogData, Camera camera,
            ClientLevel level, float partialTick, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (NoFogData.isEnabled()) {
            fogData.environmentalStart = -8.0f;
            fogData.environmentalEnd = 1000000.0f;
        }
    }
}
