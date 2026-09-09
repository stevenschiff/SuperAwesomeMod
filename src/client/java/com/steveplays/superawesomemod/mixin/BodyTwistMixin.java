package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.BodyTwistData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Legacy 1.7/1.8 body rotation: the torso leads, the legs follow late.
 *
 * <p><b>Why this does not redirect tickHeadTurn.</b> An earlier version redirected the
 * {@code Math.abs} inside {@code tickHeadTurn} the way Animatium does. ViaFabricPlus
 * ships the same feature — {@code player_rotations.MixinLivingEntity}, with redirects
 * named {@code changeBodyRotationInterpolation} and {@code alwaysRotateWhenWalkingBackwards}
 * — and only one {@code @Redirect} may own an instruction. Ours won the race, theirs was
 * skipped, and their injector then failed its own check and killed the game on startup.
 *
 * <p>So the waist is driven through {@code getMaxHeadRotationRelativeToBody} instead.
 * That is the single value vanilla clamps the head-to-body angle against (50 by
 * default), it is a whole method rather than a contested instruction, and injections
 * into it stack with other mods rather than excluding them. The slider keeps working
 * even alongside ViaFabricPlus.
 *
 * <p>The two remaining redirects carry {@code require = 0} and this mixin sits at a
 * later priority, so if another mod claims those instructions first it wins and ours
 * is skipped quietly instead of crashing. With ViaFabricPlus installed it supplies the
 * backwards-walking behaviour; without it, ours does.
 *
 * <p>Client-side and players only: {@code tick} and {@code tickHeadTurn} run on both
 * sides, and rewriting a mob's body angle server-side would reach look control and AI
 * rather than staying visual. Hitboxes are untouched either way — a bounding box is
 * axis-aligned and built from position and size, never rotation.
 */
@Mixin(value = LivingEntity.class, priority = 1500)
public abstract class BodyTwistMixin {

    /**
     * The waist. Vanilla returns 50 and drags the body around once the head leads by
     * more than that; returning the configured limit simply lets the torso lead further
     * before the legs are pulled along.
     */
    @Inject(method = "getMaxHeadRotationRelativeToBody", at = @At("HEAD"), cancellable = true)
    private void superawesomemod$widerWaist(CallbackInfoReturnable<Float> cir) {
        if (superawesomemod$active()) {
            cir.setReturnValue((float) BodyTwistData.getLeadLimit());
        }
    }

    /**
     * Vanilla flips the body a full 180 when walking backwards so the model appears to
     * walk forwards. Reporting zero fails the {@code 95 < i && i < 265} test, so you
     * walk backwards properly.
     */
    @Redirect(
        method = "tick",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;abs(F)F"),
        require = 0
    )
    private float superawesomemod$noBackwardsFlip(float value) {
        if (superawesomemod$active() && BodyTwistData.isBackwardsWalking()) {
            return 0.0F;
        }
        return Mth.abs(value);
    }

    /** Head rotation for other players snaps rather than easing, as it did in 1.7. */
    @Redirect(
        method = "lerpHeadRotationStep",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;rotLerp(DDD)D"),
        require = 0
    )
    private double superawesomemod$snapHeadRotation(double delta, double start, double end) {
        if (superawesomemod$active() && BodyTwistData.isSnapHeadRotation()) {
            return end;
        }
        return Mth.rotLerp(delta, start, end);
    }

    private boolean superawesomemod$active() {
        if (!BodyTwistData.isEnabled()) return false;
        LivingEntity self = (LivingEntity) (Object) this;
        return self.level().isClientSide() && self instanceof Player;
    }
}
