package com.steveplays.superawesomemod.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.steveplays.superawesomemod.NoFogData;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Disables world fog (underwater, nether, etc.) by replacing the WORLD fog
 * buffer with the NONE fog buffer when the No Fog feature is enabled.
 */
@Mixin(GameRenderer.class)
public class NoFogMixin {

    @Redirect(
        method = "renderLevel",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/fog/FogRenderer;getBuffer(Lnet/minecraft/client/renderer/fog/FogRenderer$FogMode;)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;",
            ordinal = 0
        )
    )
    private GpuBufferSlice superawesomemod$noFog(FogRenderer renderer, FogRenderer.FogMode mode) {
        if (NoFogData.isEnabled()) {
            return renderer.getBuffer(FogRenderer.FogMode.NONE);
        }
        return renderer.getBuffer(mode);
    }
}
