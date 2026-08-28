package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.InvincibilityData;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Invincibility: refuses damage aimed at us.
 *
 * <p>{@code hurtServer(ServerLevel, ...)} is the only place damage is resolved — the
 * signature demands a {@code ServerLevel}, so this is real exactly where our own
 * integrated server runs it and nowhere else. On someone else's server this method is
 * never ours to cancel, and no client-side alternative exists: refusing damage locally
 * would leave a full health bar on screen while the server killed us, which is worse
 * than the feature being absent.
 *
 * <p>Returning false rather than true matters — false is vanilla's "the damage did not
 * apply", which is what stops knockback, hurt animation and death from following.
 */
@Mixin(LivingEntity.class)
public abstract class InvincibilityMixin {

    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    private void superawesomemod$refuseDamage(ServerLevel level, DamageSource source,
                                              float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!InvincibilityData.isEnabled()) return;

        LivingEntity self = (LivingEntity) (Object) this;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !mc.player.getUUID().equals(self.getUUID())) return;

        cir.setReturnValue(false);
    }
}
