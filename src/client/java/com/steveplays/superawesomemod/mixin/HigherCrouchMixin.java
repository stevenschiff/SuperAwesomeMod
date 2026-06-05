package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.HigherCrouchData;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
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
 * Bypasses vanilla's smooth crouch interpolation entirely by forcing the camera
 * to the correct absolute eye height each frame. When crouching the camera
 * instantly drops to 1.54 (only 0.08 below standing); when releasing shift
 * the camera instantly returns to 1.62.
 */
@Mixin(value = Camera.class, priority = 1050)
public abstract class HigherCrouchMixin {

    @Shadow private Vec3 position;
    @Shadow protected abstract void setPosition(Vec3 pos);

    private static final double STANDING_EYE_HEIGHT = 1.62;
    private static final double CROUCH_EYE_HEIGHT = 1.54;

    @Unique private boolean wasCrouching = false;
    @Unique private int uncrouchFrames = 0;
    private static final int UNCROUCH_OVERRIDE_FRAMES = 15;

    @Inject(method = "setup", at = @At("TAIL"))
    private void superawesomemod$higherCrouch(Level level, Entity entity, boolean detached,
                                               boolean mirror, float partialTick, CallbackInfo ci) {
        if (!HigherCrouchData.isEnabled()) return;
        if (!(entity instanceof Player player)) return;
        if (detached) return;

        boolean crouching = player.isCrouching();
        double lerpedFeetY = player.yOld + (player.getY() - player.yOld) * partialTick;

        if (crouching) {
            // Force camera to our crouch eye height instantly — no interpolation.
            setPosition(new Vec3(this.position.x, lerpedFeetY + CROUCH_EYE_HEIGHT, this.position.z));
            wasCrouching = true;
            uncrouchFrames = 0;
        } else {
            if (wasCrouching) {
                wasCrouching = false;
                uncrouchFrames = UNCROUCH_OVERRIDE_FRAMES;
            }
            // Keep forcing standing eye height for several frames after uncrouching
            // to completely suppress vanilla's smooth stand-up animation.
            if (uncrouchFrames > 0) {
                setPosition(new Vec3(this.position.x, lerpedFeetY + STANDING_EYE_HEIGHT, this.position.z));
                uncrouchFrames--;
            }
        }
    }
}
