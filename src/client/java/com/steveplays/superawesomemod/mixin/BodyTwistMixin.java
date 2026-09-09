package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.BodyTwistData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Legacy 1.7/1.8 body rotation, following Animatium's approach.
 *
 * <p>An earlier attempt rewrote the render state's body angle to point along the
 * direction of travel. That turned the whole player to face where they were walking,
 * which is the opposite of the intent. The real mechanism is not to compute a body
 * angle at all — it is to change the two places vanilla forces the body to catch up
 * with the head.
 *
 * <p>All three redirects are intentionally scoped to the client and to players.
 * {@code tick} and {@code tickHeadTurn} run on both sides, and rewriting a mob's
 * {@code yBodyRot} server-side would reach look control and AI rather than staying a
 * visual change. Hitboxes are unaffected either way — a bounding box is axis-aligned
 * and built from position and size, never from rotation — so combat hitboxes and the
 * crosshair still describe exactly where a player really is.
 */
@Mixin(LivingEntity.class)
public abstract class BodyTwistMixin {

    /**
     * Vanilla flips the body a full 180 when you walk backwards, so the model appears
     * to walk forwards. Reporting zero here makes the {@code 95 < i && i < 265} test
     * fail, so the legs stay pointed the way you are actually travelling and you walk
     * backwards properly.
     */
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;abs(F)F"))
    private float superawesomemod$noBackwardsFlip(float value) {
        if (superawesomemod$active() && BodyTwistData.isBackwardsWalking()) {
            return 0.0F;
        }
        return Mth.abs(value);
    }

    /**
     * The waist. Vanilla drags the body around the moment the head leads it by more
     * than 50 degrees; this replaces that with the legacy curve — the head may lead by
     * up to the configured limit, and only past 50 do the legs begin easing after it.
     *
     * <p>Returning {@link Float#MIN_VALUE} leaves vanilla's own comparison false so it
     * skips the correction we just replaced.
     */
    @Redirect(method = "tickHeadTurn", at = @At(value = "INVOKE", target = "Ljava/lang/Math;abs(F)F"))
    private float superawesomemod$legacyWaist(float value) {
        if (!superawesomemod$active()) {
            return Math.abs(value);
        }

        LivingEntity self = (LivingEntity) (Object) this;
        float limit = BodyTwistData.getLeadLimit();
        float lead = Mth.clamp(value, -limit, limit);

        self.yBodyRot = self.getYRot() - lead;
        if (Math.abs(lead) > BodyTwistData.EASE_ABOVE) {
            self.yBodyRot += lead * 0.2F;
        }

        return Float.MIN_VALUE;
    }

    /** Head rotation for other players snaps rather than easing, as it did in 1.7. */
    @Redirect(method = "lerpHeadRotationStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;rotLerp(DDD)D"))
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
