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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Higher Crouch: raises the crouch camera height (1.8 style) so the camera
 * barely dips when sneaking.
 *
 * ONLY modifies the camera while the player is in the CROUCHING pose.
 * Crawling, swimming, elytra flight, and all other poses use completely
 * vanilla camera behaviour.
 */
@Mixin(value = Camera.class, priority = 1050)
public abstract class HigherCrouchMixin {

    @Shadow private Vec3 position;
    @Shadow protected abstract void setPosition(Vec3 pos);

    private static final double CROUCH_EYE_HEIGHT = 1.54;

    @Inject(method = "setup", at = @At("TAIL"))
    private void superawesomemod$higherCrouch(Level level, Entity entity, boolean detached,
                                               boolean mirror, float partialTick, CallbackInfo ci) {
        if (!HigherCrouchData.isEnabled()) return;
        if (!(entity instanceof Player player)) return;
        if (detached) return;

        if (player.getPose() == Pose.CROUCHING) {
            double lerpedFeetY = player.yOld + (player.getY() - player.yOld) * partialTick;
            setPosition(new Vec3(this.position.x, lerpedFeetY + CROUCH_EYE_HEIGHT, this.position.z));
        }
    }
}
