package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.FlightData;
import com.steveplays.superawesomemod.MaceDamageHandler;
import com.steveplays.superawesomemod.NoFallData;
import net.minecraft.client.Minecraft;
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
        } else if (NoFallData.isEnabled()
                   && !MaceDamageHandler.isActive(Minecraft.getInstance())) {
            // Mace Damage wants the exact opposite of this — it keeps onGround false so
            // the server holds on to the fall distance the smash bonus is scaled from.
            // Standing down here makes that precedence explicit, rather than leaving two
            // injectors to fight over one field in whatever order mixin applied them.
            ((NoFallPacketAccessor) packet).setOnGround(true);
        }
    }
}
