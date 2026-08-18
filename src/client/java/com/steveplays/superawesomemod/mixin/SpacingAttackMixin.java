package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.SpacingTracker;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Measures the gap the instant we commit to a swing.
 *
 * <p>This is the point where both positions are still exactly what we saw when we
 * decided to click, so the recorded number is the spacing we actually chose. Waiting
 * for the server's damage event instead would fold our ping into the measurement.
 *
 * <p>Observation only — it never influences whether the attack proceeds.
 */
@Mixin(MultiPlayerGameMode.class)
public abstract class SpacingAttackMixin {

    @Inject(method = "attack", at = @At("HEAD"))
    private void superawesomemod$recordSpacing(Player player, Entity target, CallbackInfo ci) {
        SpacingTracker.recordSwing(target);
    }
}
