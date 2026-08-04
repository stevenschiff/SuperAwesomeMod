package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.NoFallData;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Prevents fall damage by resetting fallDistance every tick when No Fall is enabled.
 */
@Mixin(LocalPlayer.class)
public abstract class NoFallMixin {

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void superawesomemod$noFall(CallbackInfo ci) {
        if (!NoFallData.isEnabled()) return;
        LocalPlayer self = (LocalPlayer) (Object) this;
        self.fallDistance = 0f;
    }
}
