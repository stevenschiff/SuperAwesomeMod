package com.steveplays.superawesomemod;

import com.steveplays.superawesomemod.mixin.MinecraftAutoclickerInvoker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.Items;

/**
 * Shared attack gate for Mace Timing and Swap Sync.
 *
 * <p>Both features want to stop a swing from going out at the wrong moment, and both
 * want to fire it once the moment is right. They share one gate and one pending slot
 * so that with both enabled a click is held once and released once, rather than each
 * feature queueing its own copy and the swing going out twice.
 *
 * <p>A held click is replayed rather than discarded — the point is that the hit lands,
 * not that the input disappears. It expires after {@link #PENDING_TIMEOUT_TICKS} so a
 * click can't resurface seconds later at something you are no longer aiming at.
 */
public final class AttackTimingHandler {

    private static final int PENDING_TIMEOUT_TICKS = 10;

    private static boolean pending = false;
    private static int pendingAge = 0;

    /** Ticks left on the post-swap hold. */
    private static int swapGuard = 0;
    private static int lastSlot = -1;

    private AttackTimingHandler() {}

    /**
     * Whether a swing right now should be held back. Called from the attack mixin.
     */
    public static boolean shouldHold(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null) return false;

        if (SwapSyncData.isEnabled() && swapGuard > 0) return true;

        if (MaceTimingData.isEnabled() && player.getMainHandItem().is(Items.MACE)) {
            // getAttackStrengthScale(0) is the charge as of right now, with no partial
            // tick added — the same number the crosshair's cooldown indicator draws.
            return player.getAttackStrengthScale(0.0f) < MaceTimingData.getMinStrength();
        }

        return false;
    }

    /** Records that a swing was swallowed so it can be replayed. */
    public static void hold() {
        pending = true;
        pendingAge = 0;
    }

    public static void tick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null) {
            reset();
            return;
        }

        // Track hotbar changes even while disabled, so enabling mid-game doesn't
        // inherit a stale slot and fire a spurious guard.
        int slot = player.getInventory().getSelectedSlot();
        if (slot != lastSlot) {
            lastSlot = slot;
            swapGuard = SwapSyncData.getGuardTicks();
        } else if (swapGuard > 0) {
            swapGuard--;
        }

        if (!pending) return;

        if (++pendingAge > PENDING_TIMEOUT_TICKS) {
            pending = false;
            return;
        }

        if (!shouldHold(client)) {
            pending = false;
            ((MinecraftAutoclickerInvoker) (Object) client).superawesomemod$startAttack();
        }
    }

    private static void reset() {
        pending = false;
        pendingAge = 0;
        swapGuard = 0;
        lastSlot = -1;
    }
}
