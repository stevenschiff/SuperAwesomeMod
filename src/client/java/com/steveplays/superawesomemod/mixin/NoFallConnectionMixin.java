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
 * Intercepts outgoing movement packets and rewrites the onGround flag so the
 * server never deals fall damage.
 */
@Mixin(Connection.class)
public abstract class NoFallConnectionMixin {

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"))
    private void superawesomemod$noFallPacket(Packet<?> packet, CallbackInfo ci) {
        if (!(packet instanceof ServerboundMovePlayerPacket)) return;

        if (FlightData.isEnabled()) {
            // Flight can descend far more than 3 blocks in one tick, and the server
            // charges that tick's descent as damage the instant a packet reports
            // onGround. Never report a landing while flying; FlightFallResetMixin
            // keeps the server's accumulated fall distance at zero instead.
            ((NoFallPacketAccessor) packet).setOnGround(false);
        } else if (NoFallData.isEnabled()) {
            ((NoFallPacketAccessor) packet).setOnGround(true);
        }
    }
}
