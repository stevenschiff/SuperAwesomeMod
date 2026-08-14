package com.steveplays.superawesomemod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Shared attack gate for Mace Timing, Swap Sync and the Stun Slam Optimizer.
 *
 * <p>These only ever <em>suppress</em> a swing that was going to be wasted. Nothing
 * here clicks, queues or replays — you throw every punch, and the gate's whole job is
 * to stop a mistimed one from spending the swing.
 *
 * <p>Suppressing is a real gain rather than a no-op, because {@code startAttack} resets
 * the attack-strength meter whether or not the swing connected. An early click costs
 * you the swing <em>and</em> starts the next one from an empty meter — which is what
 * makes bad hits feel like they come in pairs. Dropping the first keeps your next
 * click at full charge.
 *
 * <p>All three gates are scoped to spear, axe, mace and sword; gating anything else
 * would interfere with mining and building for no benefit.
 */
public final class AttackTimingHandler {

    private enum Weapon { NONE, SPEAR, AXE, MACE, OTHER }

    /** Flat post-swap hold from Swap Sync. */
    private static int swapGuard = 0;
    private static int lastSlot = -1;

    /** Ping-scaled hold from the Stun Slam Optimizer, armed only by a combo swap. */
    private static int comboGuard = 0;
    /** How long the optimizer's charge gate stays armed after a combo swap. */
    private static int comboWindow = 0;
    private static Weapon lastWeapon = Weapon.NONE;

    private AttackTimingHandler() {}

    /** Whether a swing right now should be suppressed. Called from the attack mixin. */
    public static boolean shouldHold(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null) return false;

        ItemStack held = player.getMainHandItem();
        if (!isCombatWeapon(held)) return false;

        if (SwapSyncData.isEnabled() && swapGuard > 0) return true;

        if (SlamOptimizerData.isEnabled()) {
            if (comboGuard > 0) return true;
            // Inside the combo, an uncharged mace hit is the one that ruins the slam.
            if (comboWindow > 0 && held.is(Items.MACE) && !isCharged(player)) return true;
        }

        if (MaceTimingData.isEnabled() && held.is(Items.MACE)) {
            return player.getAttackStrengthScale(0.0f) < MaceTimingData.getMinStrength();
        }

        return false;
    }

    public static void tick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null) {
            reset();
            return;
        }

        // Slot changes drive Swap Sync. Tracked even while the feature is off, so
        // enabling it mid-game can't inherit a stale slot and arm a phantom guard.
        int slot = player.getInventory().getSelectedSlot();
        if (slot != lastSlot) {
            lastSlot = slot;
            swapGuard = SwapSyncData.getGuardTicks();
        } else if (swapGuard > 0) {
            swapGuard--;
        }

        // Weapon changes drive the combo optimizer. Only the two transitions that make
        // up a slam arm it — reaching for an axe to chop wood leaves your clicks alone.
        Weapon weapon = classify(player.getMainHandItem());
        if (weapon != lastWeapon) {
            boolean comboStep = (lastWeapon == Weapon.SPEAR && weapon == Weapon.AXE)
                             || (lastWeapon == Weapon.AXE && weapon == Weapon.MACE);
            if (comboStep) {
                comboGuard = SlamOptimizerData.settleTicksFor(pingOf(client));
                comboWindow = SlamOptimizerData.COMBO_WINDOW_TICKS;
            }
            lastWeapon = weapon;
        } else {
            if (comboGuard > 0) comboGuard--;
            if (comboWindow > 0) comboWindow--;
        }
    }

    /** Our own latency as the server reports it, or 0 when unavailable. */
    private static int pingOf(Minecraft client) {
        if (client.player == null || client.getConnection() == null) return 0;
        PlayerInfo info = client.getConnection().getPlayerInfo(client.player.getUUID());
        return info == null ? 0 : info.getLatency();
    }

    private static boolean isCharged(LocalPlayer player) {
        return player.getAttackStrengthScale(0.0f) >= 1.0f;
    }

    private static void reset() {
        swapGuard = 0;
        lastSlot = -1;
        comboGuard = 0;
        comboWindow = 0;
        lastWeapon = Weapon.NONE;
    }

    private static Weapon classify(ItemStack stack) {
        if (stack.isEmpty()) return Weapon.NONE;
        if (stack.is(Items.MACE)) return Weapon.MACE;
        if (stack.is(ItemTags.SPEARS)) return Weapon.SPEAR;
        if (stack.is(ItemTags.AXES)) return Weapon.AXE;
        return Weapon.OTHER;
    }

    private static boolean isCombatWeapon(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.is(Items.MACE)
            || stack.is(ItemTags.SPEARS)
            || stack.is(ItemTags.AXES)
            || stack.is(ItemTags.SWORDS);
    }
}
