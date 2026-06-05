package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.CpsData;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Tracks left and right mouse clicks for the CPS counter.
 * Hooks into startAttack (left click) and startUseItem (right click).
 */
@Mixin(Minecraft.class)
public abstract class CpsClickMixin {

    @Inject(method = "startAttack", at = @At("HEAD"))
    private void superawesomemod$onLeftClick(CallbackInfoReturnable<Boolean> cir) {
        if (CpsData.isEnabled()) {
            CpsData.recordLeftClick();
        }
    }

    @Inject(method = "startUseItem", at = @At("HEAD"))
    private void superawesomemod$onRightClick(CallbackInfo ci) {
        if (CpsData.isEnabled()) {
            CpsData.recordRightClick();
        }
    }
}
