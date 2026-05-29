package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.SchematicData;
import com.steveplays.superawesomemod.SchematicPlacement;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Easy Place mixin: when auto-place is active (both toggles on),
 * prevents placing the wrong block type at a schematic position.
 * This helps the player avoid mistakes during schematic building.
 */
@Mixin(MultiPlayerGameMode.class)
public abstract class SchematicEasyPlaceMixin {

    @Inject(method = "performUseItemOn", at = @At("HEAD"), cancellable = true)
    private void superawesomemod$onUseItemOn(LocalPlayer player, BlockHitResult hitResult,
                                              CallbackInfoReturnable<InteractionResult> cir) {
        if (!SchematicData.isAutoPlaceActive()) return;

        SchematicPlacement placement = SchematicData.getCurrentPlacement();
        if (placement == null) return;

        // Determine where the new block would be placed
        BlockPos placePos = hitResult.getBlockPos().relative(hitResult.getDirection());

        // Check if this position is within the schematic bounds
        if (!placement.containsPos(placePos)) return;

        // Get the expected block at this position
        BlockState expected = placement.getBlockStateAt(placePos);
        if (expected.isAir()) return; // Schematic expects air here, allow any placement

        // Check if the player is holding the correct block type
        var mainHand = player.getMainHandItem();
        if (mainHand.isEmpty() || !(mainHand.getItem() instanceof BlockItem blockItem)) {
            return; // Not placing a block, let it through
        }

        Block heldBlock = blockItem.getBlock();
        Block expectedBlock = expected.getBlock();

        // If the held block doesn't match the expected block type, cancel placement
        if (heldBlock != expectedBlock) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }
}
