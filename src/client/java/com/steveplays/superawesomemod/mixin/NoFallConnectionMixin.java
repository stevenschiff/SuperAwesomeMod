package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.FlightData;
import com.steveplays.superawesomemod.NoFallData;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Intercepts outgoing movement packets and sets onGround = true
 * so the server never accumulates fall distance for this player.
 */
@Mixin(Connection.class)
public abstract class NoFallConnectionMixin {

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"))
    private void superawesomemod$noFallPacket(Packet<?> packet, CallbackInfo ci) {
        if ((NoFallData.isEnabled() || FlightData.isEnabled()) && packet instanceof ServerboundMovePlayerPacket) {
            ((NoFallPacketAccessor) packet).setOnGround(true);
        }
    }
}
