package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.PlayerJumpData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Multiplies the player's Y velocity immediately after the vanilla jump
 * impulse is applied, scaling jump height by the stored multiplier.
 *
 * Targets LivingEntity#jumpFromGround — called on both client (for movement
 * prediction) and server (for authoritative physics). In single-player the
 * static PlayerJumpData map is shared across both, so the command and mixin
 * stay in sync without any packet work.
 */
@Mixin(LivingEntity.class)
public abstract class JumpMixin {

    @Inject(method = "jumpFromGround", at = @At("RETURN"))
    private void onJumpFromGround(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;

        if (!(self instanceof Player player)) return;

        float multiplier = PlayerJumpData.getMultiplier(player.getUUID());
        if (multiplier == PlayerJumpData.DEFAULT) return;

        Vec3 velocity = self.getDeltaMovement();
        self.setDeltaMovement(velocity.x, velocity.y * multiplier, velocity.z);
    }
}
