package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.SpacingTracker;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Marks a recorded swing as confirmed by the server.
 *
 * <p>Deliberately a separate mixin from {@code PvpDetectorDamageMixin} rather than an
 * extra line inside it, so neither feature's behaviour depends on the other being
 * enabled or on the order the two are applied.
 *
 * <p>TAIL for the same reason the detector uses it: the vanilla method opens with
 * {@code PacketUtils.ensureRunningOnSameThread}, which reschedules off the netty
 * thread, so TAIL is only reached on the main-thread pass.
 */
@Mixin(ClientPacketListener.class)
public abstract class SpacingDamageMixin {

    @Inject(method = "handleDamageEvent", at = @At("TAIL"))
    private void superawesomemod$confirmSpacing(ClientboundDamageEventPacket packet, CallbackInfo ci) {
        int attackerId = packet.sourceCauseId();
        if (attackerId < 0) return;
        SpacingTracker.confirmSwing(attackerId, packet.entityId());
    }
}
