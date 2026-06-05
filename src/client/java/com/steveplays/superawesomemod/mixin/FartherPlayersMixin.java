package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.FartherPlayersData;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Override the shouldRender check on EntityRenderer so that player entities
 * are visible up to the configured distance (64-512 blocks) instead of the
 * default tracking range.
 */
@Mixin(EntityRenderer.class)
public abstract class FartherPlayersMixin<T extends Entity, S extends EntityRenderState> {

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void superawesomemod$fartherPlayers(T entity, Frustum frustum, double camX, double camY, double camZ,
                                                 CallbackInfoReturnable<Boolean> cir) {
        if (!FartherPlayersData.isEnabled()) return;
        if (!(entity instanceof Player)) return;

        // Always render player entities within the configured distance.
        double dx = entity.getX() - camX;
        double dy = entity.getY() - camY;
        double dz = entity.getZ() - camZ;
        double distSq = dx * dx + dy * dy + dz * dz;
        double maxDist = FartherPlayersData.getDistance();
        if (distSq <= maxDist * maxDist) {
            cir.setReturnValue(true);
        }
    }
}
