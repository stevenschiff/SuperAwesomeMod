package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.PlayerFlyData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Intercepts the server's rubber-band position correction while mod flight is active.
 *
 * When a survival player flies, the server periodically sends
 * ClientboundPlayerPositionPacket (handled by handleMovePlayer in 1.21.11)
 * to snap the player back to the ground.
 *
 * This mixin:
 *   1. Sends the teleport acknowledgment so the server stops resending the correction.
 *   2. Cancels the actual position/rotation update so the client stays flying.
 */
@Mixin(ClientPacketListener.class)
public abstract class ClientPositionCorrectionMixin {

    // The underlying network connection — inherited from ClientCommonPacketListenerImpl.
    @Shadow protected Connection connection;

    @Inject(method = "handleMovePlayer", at = @At("HEAD"), cancellable = true)
    private void cancelRubberBandWhenFlying(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (!PlayerFlyData.isEnabled(mc.player.getUUID())) return;

        // Send the teleport acknowledgment so the server considers this correction accepted
        // and stops resending it — but do NOT apply the position change.
        this.connection.send(new ServerboundAcceptTeleportationPacket(packet.id()));
        ci.cancel();
    }
}
