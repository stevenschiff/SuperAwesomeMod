package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.HigherCrouchData;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Higher Crouch: when the player crouches, the camera instantly snaps to a
 * position only slightly below standing eye height (like 1.8 PvP style).
 *
 * Vanilla standing eye = ~1.62, vanilla crouching eye = ~1.27.
 * We override crouching eye to ~1.54 (only 0.08 below standing) and
 * skip the smooth interpolation by forcing the Y position directly.
 */
@Mixin(value = Camera.class, priority = 1050)
public abstract class HigherCrouchMixin {

    @Shadow private Vec3 position;
    @Shadow protected abstract void setPosition(Vec3 pos);

    @Inject(method = "setup", at = @At("TAIL"))
    private void superawesomemod$higherCrouch(Level level, Entity entity, boolean detached,
                                               boolean mirror, float partialTick, CallbackInfo ci) {
        if (!HigherCrouchData.isEnabled()) return;
        if (!(entity instanceof Player player)) return;
        if (detached) return; // Only in first person

        // Only adjust when the player is actually in a crouching pose
        if (!player.isCrouching()) return;

        // Standing eye height is ~1.62. Vanilla crouching eye is ~1.27.
        // We want crouching eye at ~1.54 (only a small dip).
        // Force the Y directly to player feet + desired eye height,
        // which skips the vanilla smooth interpolation (instant crouch).
        double feetY = player.getY();
        double desiredEyeY = feetY + 1.54;

        setPosition(new Vec3(this.position.x, desiredEyeY, this.position.z));
    }
}
