package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.BodyTwistData;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Lets the legs point where a player is travelling while the torso and head stay on
 * whatever they are aiming at.
 *
 * <p>Vanilla ties the two together with one number: {@code
 * LivingEntity.getMaxHeadRotationRelativeToBody()} returns 50, and the body is dragged
 * around whenever the head drifts further than that. Widening it, and pointing the body
 * along the direction of travel, is the whole effect.
 *
 * <p><b>Why this writes the render state and not the entity.</b> The user's condition
 * was that combat hitboxes and the crosshair stay honest no matter how a player looks.
 * {@code bodyRot} on the render state is consumed only when drawing the model, so
 * nothing here can reach hit detection: an entity's bounding box is axis-aligned and
 * derived from position and size, never from any rotation, and the crosshair picks its
 * target by ray-casting that same box. Editing the entity's real {@code yBodyRot} would
 * have been the tempting shortcut and would have muddied that guarantee for no gain.
 *
 * <p>Applies to every player on screen, so a friend walking past visibly twists too.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class BodyTwistMixin {

    /** Below this squared per-tick movement we treat the player as standing still. */
    private static final double MOVING_THRESHOLD_SQR = 1.0E-6;

    @Inject(
        method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
        at = @At("TAIL")
    )
    private void superawesomemod$twistBody(LivingEntity entity, LivingEntityRenderState state,
                                           float partialTick, CallbackInfo ci) {
        if (!BodyTwistData.isEnabled()) return;
        if (!(entity instanceof Player)) return;

        float body = state.bodyRot;

        // Where they are actually travelling this tick. Position delta rather than
        // delta movement, because remote players are interpolated and their velocity
        // is not always populated the way the local player's is.
        double dx = entity.getX() - entity.xo;
        double dz = entity.getZ() - entity.zo;
        if (dx * dx + dz * dz > MOVING_THRESHOLD_SQR) {
            body = (float) Math.toDegrees(Math.atan2(-dx, dz));
        }
        // Standing still keeps vanilla's body angle, so the legs settle where they are
        // instead of snapping to a stale heading the moment someone stops.

        // Let the head lead by up to the configured angle before the legs give in.
        float lead = Mth.wrapDegrees(state.yRot - body);
        int limit = BodyTwistData.getTwistLimit();
        if (Math.abs(lead) > limit) {
            body = state.yRot - Math.signum(lead) * limit;
        }

        state.bodyRot = body;
    }
}
