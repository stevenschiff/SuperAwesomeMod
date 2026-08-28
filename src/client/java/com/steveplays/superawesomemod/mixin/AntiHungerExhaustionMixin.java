package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.AntiHungerData;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Freeze Hunger: stops exhaustion accruing at all, covering the sources the sprint
 * spoof cannot — attacking bills a flat {@code 0.1F} regardless of sprint state.
 *
 * <p>Hooks {@code Player.causeFoodExhaustion} rather than {@code FoodData.addExhaustion}
 * because {@code FoodData} has no idea whose hunger it is, while the {@code Player}
 * wrapper does — the same reason the potion saver needed an owner-aware seam.
 *
 * <p>Cancelling exhaustion rather than {@code FoodData.tick} keeps saturation healing
 * intact; freezing the tick would stop starving and stop regenerating together.
 *
 * <p>Host-only by nature: {@code FoodData.tick} takes a {@code ServerPlayer}, so on a
 * server we do not run this method is never ours to cancel.
 */
@Mixin(Player.class)
public abstract class AntiHungerExhaustionMixin {

    @Inject(method = "causeFoodExhaustion", at = @At("HEAD"), cancellable = true)
    private void superawesomemod$freezeHunger(float exhaustion, CallbackInfo ci) {
        if (!AntiHungerData.isFreezeHungerActive()) return;

        Player self = (Player) (Object) this;
        if (self.level().isClientSide()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !mc.player.getUUID().equals(self.getUUID())) return;

        ci.cancel();
    }
}
