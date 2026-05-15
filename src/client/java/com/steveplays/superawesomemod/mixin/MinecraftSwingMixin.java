package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.OldPvpData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftSwingMixin {

    @Shadow public LocalPlayer player;
    @Shadow public Options options;

    /**
     * Swing while eating / drawing bow: vanilla's handleKeybinds drains all
     * attack clicks when isUsingItem() is true, so startAttack is never called.
     * We intercept at the top of handleKeybinds to consume one attack click
     * per tick and fire the swing animation instead.
     */
    @Inject(method = "handleKeybinds", at = @At("HEAD"))
    private void superawesomemod$swingWhileEating(CallbackInfo ci) {
        if (player == null) return;
        if (!OldPvpData.isSwingWhileUsingEnabled()) return;
        if (!player.isUsingItem()) return;

        if (options.keyAttack.consumeClick()) {
            player.swing(InteractionHand.MAIN_HAND);
        }
    }

    /**
     * Swing while custom sword blocking: startAttack IS called here because
     * isUsingItem() is false for swords. We fire the swing and cancel the
     * actual attack.
     */
    @Inject(method = "startAttack", at = @At("HEAD"))
    private void superawesomemod$swingWhileBlocking(CallbackInfoReturnable<Boolean> cir) {
        if (player == null) return;
        if (!OldPvpData.isSwingWhileUsingEnabled()) return;

        if (OldPvpData.isCustomBlocking()) {
            player.swing(InteractionHand.MAIN_HAND);
            cir.setReturnValue(false);
        }
    }
}
