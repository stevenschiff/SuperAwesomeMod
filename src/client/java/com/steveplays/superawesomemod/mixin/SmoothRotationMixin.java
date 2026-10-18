package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.RotationHelper;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Drives the auto-miner's camera once per frame.
 *
 * <p>At the head of {@code render}, which is before the camera is positioned, so the
 * angles written here are the ones this frame draws. Easing on the 20 Hz client tick
 * instead would cap the motion at 20 visible steps a second.
 */
@Mixin(GameRenderer.class)
public abstract class SmoothRotationMixin {

    @Inject(method = "render", at = @At("HEAD"))
    private void superawesomemod$smoothRotation(DeltaTracker deltaTracker, boolean bl, CallbackInfo ci) {
        RotationHelper.renderStep(Minecraft.getInstance());
    }
}
