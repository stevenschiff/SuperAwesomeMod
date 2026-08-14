package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.FastActionData;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fast Place: clears the 4-tick cooldown vanilla puts between held right-clicks,
 * so holding use fires every tick — 20 per second instead of 5.
 *
 * <p>Applies to whatever is in hand, by design. {@code rightClickDelay} is the only
 * thing gating the held-repeat path, and it doesn't know what you're holding, so the
 * same clearing that gives fast building also empties a stack of rockets or pearls
 * while right-click is held. That is the cost of the feature rather than a bug in it —
 * it is off unless Fast Actions is on, and Fast Actions is not part of Enable All.
 */
@Mixin(Minecraft.class)
public abstract class FastActionPlaceMixin {

    @Shadow private int rightClickDelay;

    @Inject(method = "tick", at = @At("HEAD"))
    private void superawesomemod$fastPlace(CallbackInfo ci) {
        if (FastActionData.isFastPlace()) {
            this.rightClickDelay = 0;
        }
    }
}
