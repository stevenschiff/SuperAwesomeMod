package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.HigherCrouchData;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Higher Crouch: instant crouch/uncrouch with barely any camera dip (1.8 style).
 *
 * Only activates when the player is in the CROUCHING pose — crawling, swimming,
 * elytra flight, and other poses are left at their vanilla camera height.
 *
 * Also syncs the entity's eye height to the camera position so the raycast
 * (crosshair target) matches what the player sees.
 */
@Mixin(value = Camera.class, priority = 1050)
public abstract class HigherCrouchMixin {

    @Shadow private Vec3 position;
    @Shadow protected abstract void setPosition(Vec3 pos);

    private static final double STANDING_EYE_HEIGHT = 1.62;
    private static final double CROUCH_EYE_HEIGHT = 1.54;

    @Unique private boolean hasCrouchedOnce = false;

    @Inject(method = "setup", at = @At("TAIL"))
    private void superawesomemod$higherCrouch(Level level, Entity entity, boolean detached,
                                               boolean mirror, float partialTick, CallbackInfo ci) {
        if (!HigherCrouchData.isEnabled()) return;
        if (!(entity instanceof Player player)) return;
        if (detached) return;

        Pose pose = player.getPose();
        double lerpedFeetY = player.yOld + (player.getY() - player.yOld) * partialTick;

        if (pose == Pose.CROUCHING) {
            // Force camera to our crouch eye height instantly — no interpolation.
            setPosition(new Vec3(this.position.x, lerpedFeetY + CROUCH_EYE_HEIGHT, this.position.z));
            // Sync entity eye height so raycasts start from the same position as the camera.
            ((EntityEyeHeightAccessor) player).superawesomemod$setEyeHeight((float) CROUCH_EYE_HEIGHT);
            hasCrouchedOnce = true;
        } else if (hasCrouchedOnce && pose == Pose.STANDING) {
            // Only force standing height when actually standing — not when
            // crawling (SWIMMING), flying (FALL_FLYING), or any other non-standing pose.
            double standingEyeY = lerpedFeetY + STANDING_EYE_HEIGHT;
            if (this.position.y < standingEyeY - 0.001) {
                setPosition(new Vec3(this.position.x, standingEyeY, this.position.z));
            }
        }
    }
}
