package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.MaceDamageHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Holds onGround false while a mace is held, so the server never resets the fall
 * distance the smash bonus is calculated from.
 *
 * <p>Deliberately the opposite of what {@code NoFallConnectionMixin} does, which is
 * why that mixin now stands down while this one is active — a shared field being
 * written to true and false by two injectors in an unspecified order would make both
 * features behave differently depending on mixin load order.
 */
@Mixin(Connection.class)
public abstract class MaceDamageMixin {

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"))
    private void superawesomemod$keepFallDistance(Packet<?> packet, CallbackInfo ci) {
        if (!(packet instanceof ServerboundMovePlayerPacket)) return;
        if (!MaceDamageHandler.isActive(Minecraft.getInstance())) return;

        ((NoFallPacketAccessor) packet).setOnGround(false);
    }
}
