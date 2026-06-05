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
 * Instead of setting an absolute Y from entity position (which causes choppiness
 * during jumps), this adds an offset to vanilla's already-interpolated camera Y.
 * Vanilla smoothly interpolates the camera to crouching eye (~1.27). We add the
 * difference to bring it up to our target (~1.54), giving +0.27 offset.
 * On uncrouch, vanilla interpolates back up from 1.27 — we counteract by removing
 * our offset instantly so there's no snap-down.
 */
@Mixin(value = Camera.class, priority = 1050)
public abstract class HigherCrouchMixin {

    @Shadow private Vec3 position;
    @Shadow protected abstract void setPosition(Vec3 pos);

    // How much higher our crouch eye is compared to vanilla's crouch eye.
    // Vanilla crouch eye ~1.27, we want ~1.54, so offset = +0.27.
    private static final double CROUCH_Y_OFFSET = 0.27;

    @Unique private boolean wasCrouching = false;
    @Unique private int uncrouchFrames = 0;
    private static final int UNCROUCH_OVERRIDE_FRAMES = 10;

    @Inject(method = "setup", at = @At("TAIL"))
    private void superawesomemod$higherCrouch(Level level, Entity entity, boolean detached,
                                               boolean mirror, float partialTick, CallbackInfo ci) {
        if (!HigherCrouchData.isEnabled()) return;
        if (!(entity instanceof Player player)) return;
        if (detached) return;

        boolean crouching = player.isCrouching();

        if (crouching) {
            // Vanilla has already set the camera to its interpolated crouch position.
            // Just bump it up by our offset. This preserves vanilla's smooth handling
            // of jumps, movement, etc. — we only change the crouch depth.
            setPosition(new Vec3(this.position.x, this.position.y + CROUCH_Y_OFFSET, this.position.z));
            wasCrouching = true;
            uncrouchFrames = 0;
        } else if (wasCrouching) {
            wasCrouching = false;
            uncrouchFrames = UNCROUCH_OVERRIDE_FRAMES;
        }

        if (!crouching && uncrouchFrames > 0) {
            // Vanilla is smoothly interpolating from crouch eye back to standing.
            // Its current Y is somewhere between 1.27 and 1.62. But standing is
            // the correct final position — just force it there by adding whatever
            // vanilla still has left to interpolate, making uncrouch instant.
            // The interpolated player eye Y from vanilla is this.position.y.
            // Standing eye Y would be entity lerped Y + 1.62.
            // We compute the lerped feet Y from vanilla's position minus whatever
            // eye height vanilla used, but simpler: just use the entity's
            // interpolated position.
            double lerpedFeetY = player.yOld + (player.getY() - player.yOld) * partialTick;
            double standingEyeY = lerpedFeetY + 1.62;
            if (this.position.y < standingEyeY - 0.001) {
                setPosition(new Vec3(this.position.x, standingEyeY, this.position.z));
            }
            uncrouchFrames--;
        }
    }
}
