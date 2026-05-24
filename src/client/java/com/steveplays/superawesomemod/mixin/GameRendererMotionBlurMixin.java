package com.steveplays.superawesomemod.mixin;

import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import com.steveplays.superawesomemod.MotionBlurRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMotionBlurMixin {

    @Shadow @Final private CrossFrameResourcePool resourcePool;

    @Inject(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/GameRenderer;doEntityOutline()V",
            shift = At.Shift.AFTER))
    private void superawesomemod$afterEntityOutline(DeltaTracker deltaTracker, boolean bl, CallbackInfo ci) {
        MotionBlurRenderer.onRenderPost(this.resourcePool);
    }
}
