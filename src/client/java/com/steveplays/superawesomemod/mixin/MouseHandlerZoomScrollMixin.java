package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.ZoomData;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerZoomScrollMixin {

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void superawesomemod$interceptZoomScroll(long window, double xOffset, double yOffset,
                                                     CallbackInfo ci) {
        if (ZoomData.isKeyHeld()) {
            ZoomData.onScroll(yOffset);
            ci.cancel();
        }
    }
}
