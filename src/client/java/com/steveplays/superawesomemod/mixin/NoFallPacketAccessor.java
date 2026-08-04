package com.steveplays.superawesomemod.mixin;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor to mutate the onGround flag on outgoing movement packets.
 */
@Mixin(ServerboundMovePlayerPacket.class)
public interface NoFallPacketAccessor {
    @Accessor("onGround")
    @Mutable
    void setOnGround(boolean onGround);
}
