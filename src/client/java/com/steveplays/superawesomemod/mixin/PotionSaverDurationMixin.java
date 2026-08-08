package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.PotionSaverData;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Potion Saver, part two: skips the countdown for beneficial effects on us.
 *
 * <p>Cancelling here rather than restoring the duration afterwards means the timer
 * is never decremented in the first place, so an effect can't expire and be removed
 * mid-loop, and the recursive call this method makes into {@code hiddenEffect}
 * (a weaker same-effect instance waiting underneath, e.g. beacon Speed I sitting
 * under potion Speed II) is skipped along with it.
 *
 * <p>Split by {@link MobEffectCategory}, which is what the game already uses to
 * colour effect names in tooltips: only BENEFICIAL is held. HARMFUL runs out
 * normally — a Turtle Master splash keeps its Resistance and drops its Slowness —
 * and so does NEUTRAL, since effects like Glowing aren't a buff worth keeping.
 */
@Mixin(MobEffectInstance.class)
public abstract class PotionSaverDurationMixin {

    @Inject(method = "tickDownDuration", at = @At("HEAD"), cancellable = true)
    private void superawesomemod$holdDuration(CallbackInfo ci) {
        if (!PotionSaverData.isEnabled() || !PotionSaverData.isHolding()) return;

        MobEffectInstance self = (MobEffectInstance) (Object) this;
        if (self.getEffect().value().getCategory() != MobEffectCategory.BENEFICIAL) return;

        ci.cancel();
    }
}
