package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.MotionBlurRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMotionBlurMixin {

    @Inject(method = "renderLevel", at = @At("TAIL"))
    private void superawesomemod$applyMotionBlur(DeltaTracker deltaTracker, CallbackInfo ci) {
        MotionBlurRenderer.applyMotionBlur();
    }
}
