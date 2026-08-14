package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.AttackTimingHandler;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Holds back swings that would be wasted, for Mace Timing and Swap Sync.
 *
 * <p>Cancelling here rather than after the fact matters: once {@code startAttack} runs
 * the attack packet is already gone and the strength meter has already been reset, so
 * an early click has spent the swing whether or not it did any damage. That reset is
 * what makes mishits feel like they alternate — the swing that follows an early one
 * starts from an empty meter too.
 */
@Mixin(Minecraft.class)
public abstract class AttackTimingMixin {

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void superawesomemod$holdAttack(CallbackInfoReturnable<Boolean> cir) {
        Minecraft client = (Minecraft) (Object) this;
        if (!AttackTimingHandler.shouldHold(client)) return;

        AttackTimingHandler.hold();
        cir.setReturnValue(false);
    }
}
