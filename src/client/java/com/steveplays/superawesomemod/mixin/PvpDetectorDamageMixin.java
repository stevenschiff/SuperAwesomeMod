package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.PvpDetectorTracker;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Capture every damage event the client is informed about and forward it to
 * the PvP detector tracker.
 *
 * Injecting at TAIL guarantees we run on the main thread — the vanilla method
 * begins with {@code PacketUtils.ensureRunningOnSameThread} which throws and
 * reschedules when invoked on the netty thread, so TAIL is only reached on
 * the main-thread invocation.
 */
@Mixin(ClientPacketListener.class)
public abstract class PvpDetectorDamageMixin {

    @Inject(method = "handleDamageEvent", at = @At("TAIL"))
    private void superawesomemod$onDamageEvent(ClientboundDamageEventPacket packet, CallbackInfo ci) {
        int attackerId = packet.sourceCauseId();
        if (attackerId < 0) return;
        PvpDetectorTracker.recordHit(attackerId, packet.entityId());
    }
}
