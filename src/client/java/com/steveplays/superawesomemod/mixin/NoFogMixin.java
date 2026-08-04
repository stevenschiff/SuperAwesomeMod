package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.NoFogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Disables world fog by replacing the WORLD fog mode with NONE
 * when the No Fog feature is enabled.
 */
@Mixin(FogRenderer.class)
public class NoFogMixin {

    @ModifyVariable(method = "getBuffer", at = @At("HEAD"), argsOnly = true)
    private FogRenderer.FogMode superawesomemod$noFog(FogRenderer.FogMode mode) {
        if (NoFogData.isEnabled() && mode == FogRenderer.FogMode.WORLD) {
            return FogRenderer.FogMode.NONE;
        }
        return mode;
    }
}
