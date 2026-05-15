package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.OldPvpData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftSwingMixin {

    @Shadow public LocalPlayer player;

    @Inject(method = "startAttack", at = @At("HEAD"))
    private void superawesomemod$swingWhileUsing(CallbackInfoReturnable<Boolean> cir) {
        if (player == null) return;
        if (!OldPvpData.isSwingWhileUsingEnabled()) return;

        // Swing while eating / drawing bow / using any item
        if (player.isUsingItem()) {
            player.swing(InteractionHand.MAIN_HAND);
            return;
        }

        // Swing while custom sword blocking (prevent actual attack)
        if (OldPvpData.isCustomBlocking()) {
            player.swing(InteractionHand.MAIN_HAND);
            cir.setReturnValue(false);
        }
    }
}
