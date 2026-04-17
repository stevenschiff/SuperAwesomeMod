package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.PlayerFlyData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPositionCorrectionMixin {

    @Inject(method = "handleMovePlayer", at = @At("HEAD"), cancellable = true)
    private void cancelRubberBandWhenFlying(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.player.connection == null) return;
        if (!PlayerFlyData.isEnabled(mc.player.getUUID())) return;

        // Ack the teleport so the server stops resending, but don't apply the position.
        mc.player.connection.send(new ServerboundAcceptTeleportationPacket(packet.id()));
        ci.cancel();
    }
}
