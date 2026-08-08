package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.PotionSaverData;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Potion Saver, part one: marks the window in which our own effects are ticked.
 *
 * <p>{@code tickEffects} is the only place that reaches the duration countdown, and
 * it is the last point that still knows which entity the effects belong to —
 * {@code MobEffectInstance} itself has no owner. Flagging the window here is what
 * lets {@link PotionSaverDurationMixin} hold only our timers, rather than every
 * beneficial effect in the world including the ones on witches and other players.
 * It covers both sides: the server branch owns the real duration, the client branch
 * owns the number drawn in the inventory.
 */
@Mixin(LivingEntity.class)
public abstract class PotionSaverEntityMixin {

    @Inject(method = "tickEffects", at = @At("HEAD"))
    private void superawesomemod$beginPotionSaver(CallbackInfo ci) {
        if (!PotionSaverData.isEnabled()) return;

        Minecraft mc = Minecraft.getInstance();
        // On a server we don't run, only our own display copy is here to freeze —
        // the real timer keeps running and the effect still expires. Holding the
        // number still would just make it look like the feature works. Leave it
        // alone so the countdown on screen is the truth.
        if (!mc.hasSingleplayerServer()) return;

        LivingEntity self = (LivingEntity) (Object) this;
        if (mc.player == null || !mc.player.getUUID().equals(self.getUUID())) return;

        PotionSaverData.setHolding(true);
    }

    @Inject(method = "tickEffects", at = @At("TAIL"))
    private void superawesomemod$endPotionSaver(CallbackInfo ci) {
        PotionSaverData.setHolding(false);
    }
}
