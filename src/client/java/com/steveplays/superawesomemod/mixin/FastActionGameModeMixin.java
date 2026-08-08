package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.FastActionData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fast Break and Auto Tool.
 *
 * <p>Vanilla parks a 5-tick {@code destroyDelay} after each block, and
 * {@code continueDestroyBlock} burns a tick doing nothing until it drains. That
 * delay is client-side only, so clearing it just removes dead time between blocks.
 * Breaking a single block still takes as long as it takes — the server tracks that
 * progress itself and would reject anything faster.
 */
@Mixin(MultiPlayerGameMode.class)
public abstract class FastActionGameModeMixin {

    @Shadow private int destroyDelay;

    @Inject(method = "continueDestroyBlock", at = @At("HEAD"))
    private void superawesomemod$fastBreak(BlockPos pos, Direction face, CallbackInfoReturnable<Boolean> cir) {
        if (FastActionData.isFastBreak()) {
            this.destroyDelay = 0;
        }
    }

    @Inject(method = "startDestroyBlock", at = @At("HEAD"))
    private void superawesomemod$autoTool(BlockPos pos, Direction face, CallbackInfoReturnable<Boolean> cir) {
        if (!FastActionData.isAutoTool()) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null) return;

        BlockState state = mc.level.getBlockState(pos);
        if (state.isAir()) return;

        Inventory inventory = player.getInventory();
        int bestSlot = inventory.getSelectedSlot();
        float bestSpeed = inventory.getItem(bestSlot).getDestroySpeed(state);

        for (int i = 0; i < Inventory.getSelectionSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) continue;
            float speed = stack.getDestroySpeed(state);
            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = i;
            }
        }

        // Only switch on a strict improvement, so an empty hand or an equally good
        // tool doesn't yank the selected slot around while building.
        if (bestSlot != inventory.getSelectedSlot()) {
            inventory.setSelectedSlot(bestSlot);
        }
    }
}
