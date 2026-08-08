package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.NoSlowData;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * NoSlow: keeps full movement while eating, drinking, blocking or drawing a bow.
 *
 * <p>Both halves of the slowdown live in single-purpose private methods on
 * {@code LocalPlayer}, so this overrides those rather than the magic numbers they
 * feed: {@code itemUseSpeedMultiplier} is what {@code modifyInput} scales the
 * movement vector by, and {@code isSlowDueToUsingItem} is what forbids sprinting.
 *
 * <p>Movement is client-authoritative — the server takes the positions we report —
 * so unlike a speed hack there is no threshold here to trip.
 */
@Mixin(LocalPlayer.class)
public abstract class NoSlowMixin {

    @Inject(method = "itemUseSpeedMultiplier", at = @At("HEAD"), cancellable = true)
    private void superawesomemod$noUseSlowdown(CallbackInfoReturnable<Float> cir) {
        if (NoSlowData.isEnabled()) cir.setReturnValue(1.0F);
    }

    @Inject(method = "isSlowDueToUsingItem", at = @At("HEAD"), cancellable = true)
    private void superawesomemod$allowSprinting(CallbackInfoReturnable<Boolean> cir) {
        if (NoSlowData.isEnabled()) cir.setReturnValue(false);
    }
}
