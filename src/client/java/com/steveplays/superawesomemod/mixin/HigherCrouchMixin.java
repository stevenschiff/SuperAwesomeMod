package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.HigherCrouchData;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes the crouch height higher so the player barely ducks.
 * Vanilla crouching dims are ~1.5 tall with ~1.27 eye height.
 * We override to 1.65 tall with ~1.54 eye height for a subtle dip.
 */
@Mixin(Entity.class)
public abstract class HigherCrouchMixin {

    @Inject(method = "getDimensions", at = @At("RETURN"), cancellable = true)
    private void superawesomemod$higherCrouch(Pose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        if (!HigherCrouchData.isEnabled()) return;
        if (!((Object) this instanceof Player)) return;
        if (pose != Pose.CROUCHING) return;

        EntityDimensions original = cir.getReturnValue();
        // Raise crouch height from ~1.5 to 1.65, eye height from ~1.27 to 1.54
        float newHeight = 1.65f;
        float newEyeHeight = 1.54f;
        cir.setReturnValue(EntityDimensions.scalable(original.width(), newHeight)
            .withEyeHeight(newEyeHeight));
    }
}
