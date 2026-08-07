package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.FlightData;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Keeps the server's copy of our fall distance at zero while Flight is on.
 *
 * <p>Zeroing {@code fallDistance} client-side does nothing on its own — fall damage
 * is dealt by the server, which adds each movement packet's downward delta to its
 * own fall distance and, in the same call, cashes it in if the packet says onGround.
 * Descending more than the 3-block safe-fall distance in a single tick therefore
 * hurts every tick regardless of what the fall distance was before, which is why
 * fast flight hurts and slow flight doesn't. {@link NoFallConnectionMixin} keeps
 * onGround false while flying so that branch never runs; this sends a follow-up
 * packet 0.001 blocks higher than where we actually are, which is the one path in
 * {@code handleMovePlayer} that clears the accumulated distance without a landing.
 * Without it the whole descent would be waiting to land the moment Flight is
 * switched back off.
 */
@Mixin(LocalPlayer.class)
public abstract class FlightFallResetMixin {

    @Unique
    private double superawesomemod$lastY = Double.NaN;

    @Inject(method = "sendPosition", at = @At("TAIL"))
    private void superawesomemod$resetServerFallDistance(CallbackInfo ci) {
        LocalPlayer self = (LocalPlayer) (Object) this;

        double y = self.getY();
        double previousY = superawesomemod$lastY;
        superawesomemod$lastY = y;

        if (!FlightData.isEnabled()) return;
        // Fall distance only builds up while descending, so only clear it then —
        // nudging upwards against a ceiling would get the packet rejected.
        if (Double.isNaN(previousY) || y >= previousY) return;

        self.connection.send(new ServerboundMovePlayerPacket.Pos(
            self.getX(), y + 0.001, self.getZ(), false, self.horizontalCollision));
    }
}
