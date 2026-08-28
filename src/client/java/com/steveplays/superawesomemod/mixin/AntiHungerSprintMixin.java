package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.AntiHungerData;
import com.steveplays.superawesomemod.AntiHungerHandler;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Suppresses vanilla's own sprint reporting so {@link AntiHungerHandler} is the only
 * thing telling the server whether we sprint.
 *
 * <p>Same interception point as {@code NoFallConnectionMixin}. Both sprint actions are
 * dropped, not just START: leaving STOP to vanilla would let it correct the server back
 * to the truth at arbitrary moments and undo the spoof mid-run.
 */
@Mixin(Connection.class)
public abstract class AntiHungerSprintMixin {

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void superawesomemod$swallowSprintPackets(Packet<?> packet, CallbackInfo ci) {
        if (!AntiHungerData.isWalkReportActive()) return;
        if (AntiHungerHandler.isSendingOwn()) return;
        if (!(packet instanceof ServerboundPlayerCommandPacket command)) return;

        ServerboundPlayerCommandPacket.Action action = command.getAction();
        if (action == ServerboundPlayerCommandPacket.Action.START_SPRINTING
            || action == ServerboundPlayerCommandPacket.Action.STOP_SPRINTING) {
            ci.cancel();
        }
    }
}
