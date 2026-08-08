package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.AntiKnockbackData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Anti-Knockback: scales down the two ways the server pushes us around.
 *
 * <p>Knockback is one of the few combat values the server hands to the client to
 * apply rather than resolving itself — it sends a velocity and trusts us with it,
 * and never checks that we used it. Scaling it here is invisible from the outside,
 * unlike the effect durations and XP amounts the server computes for itself.
 */
@Mixin(ClientPacketListener.class)
public abstract class AntiKnockbackMixin {

    /** Hits, projectiles and anything else that sends an explicit velocity. */
    @Redirect(
        method = "handleSetEntityMotion",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;lerpMotion(Lnet/minecraft/world/phys/Vec3;)V")
    )
    private void superawesomemod$scaleVelocity(Entity entity, Vec3 movement) {
        // Other entities' velocities are left alone — scaling those would just
        // desync how we see them move.
        boolean self = entity == Minecraft.getInstance().player;
        entity.lerpMotion(self ? AntiKnockbackData.scale(movement) : movement);
    }

    /** Explosions carry their player knockback in the packet instead. */
    @Redirect(
        method = "handleExplosion",
        at = @At(value = "INVOKE", target = "Ljava/util/Optional;ifPresent(Ljava/util/function/Consumer;)V")
    )
    private void superawesomemod$scaleExplosionKnockback(Optional<Vec3> knockback, Consumer<Vec3> apply) {
        knockback.map(AntiKnockbackData::scale).ifPresent(apply);
    }
}
