package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.ZoomData;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererZoomMixin {

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void superawesomemod$applyZoom(Camera camera, float partialTick, boolean useFovSetting,
                                            CallbackInfoReturnable<Float> cir) {
        if (!ZoomData.isActive()) return;
        cir.setReturnValue(cir.getReturnValueF() * ZoomData.getMultiplier(partialTick));
    }
}
