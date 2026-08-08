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
 * so holding use places every tick instead of every fifth one. Client-side rate
 * limiting only — the server accepts the placements either way.
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
