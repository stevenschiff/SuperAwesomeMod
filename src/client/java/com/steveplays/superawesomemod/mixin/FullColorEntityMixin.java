package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.CombatHitboxData;
import com.steveplays.superawesomemod.CombatReach;
import com.steveplays.superawesomemod.FullColorTinted;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Full Colour, body half: decides the tint while the entity is still in hand, then
 * applies it when the model is drawn.
 *
 * <p>Both halves target explicit descriptors rather than bare method names, because
 * each of these has a synthetic bridge overload alongside it and a bare name would be
 * ambiguous.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class FullColorEntityMixin {

    @Inject(
        method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
        at = @At("TAIL")
    )
    private void superawesomemod$markFullColor(LivingEntity entity, LivingEntityRenderState state,
                                               float partialTick, CallbackInfo ci) {
        ((FullColorTinted) state).superawesomemod$setFullColorTint(superawesomemod$tintFor(entity));
    }

    @Inject(
        method = "getModelTint(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;)I",
        at = @At("HEAD"),
        cancellable = true
    )
    private void superawesomemod$applyFullColor(LivingEntityRenderState state,
                                                CallbackInfoReturnable<Integer> cir) {
        int tint = ((FullColorTinted) state).superawesomemod$getFullColorTint();
        if (tint != 0) cir.setReturnValue(tint);
    }

    /**
     * The in-range colour when this entity is one we could hit right now, otherwise 0.
     * Mirrors the filters {@code CombatHitboxRenderer} applies, so the fill and the
     * outline never disagree about who is in range.
     */
    private static int superawesomemod$tintFor(LivingEntity entity) {
        if (!CombatHitboxData.isEnabled() || !CombatHitboxData.isFullColor()) return 0;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer self = mc.player;
        if (self == null || entity == self) return 0;
        if (!entity.isAttackable()) return 0;
        if (CombatHitboxData.isPlayersOnly() && !(entity instanceof Player)) return 0;
        if (!CombatHitboxData.isShowInvisible() && entity.isInvisible()) return 0;

        double reach = CombatReach.attackReach(self);
        double gapSqr = CombatReach.gapSqr(self.getEyePosition(), entity.getBoundingBox());
        return gapSqr <= reach * reach ? CombatHitboxData.getInRangeARGB() : 0;
    }
}
