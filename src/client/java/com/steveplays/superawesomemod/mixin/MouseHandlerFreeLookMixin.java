package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.FreeLookData;
import com.steveplays.superawesomemod.ModKeybindings;
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
        if (ModKeybindings.freeLook.isDown()) {
            FreeLookData.addDelta((float) yaw, (float) pitch);
        } else {
            player.turn(yaw, pitch);
        }
    }
}
