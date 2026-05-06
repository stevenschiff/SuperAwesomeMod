package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.FreeLookData;
import com.steveplays.superawesomemod.FreecamData;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MouseHandler.class)
public class MouseHandlerFreeLookMixin {

    @Redirect(
        method = "turnPlayer",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V")
    )
    private void redirectTurn(LocalPlayer player, double yaw, double pitch) {
        if (FreecamData.isEnabled()) {
            // Entity.turn() applies a 0.15 multiplier internally; replicate that
            // here so freecam mouse sensitivity matches vanilla feel.
            FreecamData.addYaw((float) (yaw * 0.15));
            FreecamData.addPitch((float) (pitch * 0.15));
            return;
        }
        if (FreeLookData.isActive()) {
            FreeLookData.addDelta((float) yaw, (float) pitch);
        } else {
            player.turn(yaw, pitch);
        }
    }
}
