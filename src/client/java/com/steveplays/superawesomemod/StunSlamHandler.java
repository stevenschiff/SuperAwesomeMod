package com.steveplays.superawesomemod;

import com.steveplays.superawesomemod.mixin.MinecraftAutoclickerInvoker;
import net.minecraft.client.Minecraft;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Stun Slam: fires the combo's clicks so only the weapon swaps are manual.
 *
 * <p>Swap assist watches for the two <em>transitions</em> in the combo — spear to axe,
 * and axe to mace — rather than simply "an axe is now held". Triggering on the item
 * alone would fire on every unrelated hotbar swap; requiring the previous weapon means
 * reaching for an axe to chop wood stays silent.
 *
 * <p>Queued clicks drain one per tick. That is what "as fast as possible" means in
 * practice: the server processes one attack per tick, so same-tick duplicates are
 * thrown away. The cost is that the second mace click lands on a nearly empty attack
 * cooldown — it supplies knockback and the stun, not damage.
 */
public final class StunSlamHandler {

    /** Which weapon the combo is currently on. */
    private enum Weapon { NONE, SPEAR, AXE, MACE }

    private static Weapon lastHeld = Weapon.NONE;
    private static int pendingClicks = 0;

    // Macro state: -1 when idle, otherwise the step index.
    private static int macroStep = -1;
    private static int macroSpearSlot, macroAxeSlot, macroMaceSlot;

    private StunSlamHandler() {}

    public static void tick(Minecraft client) {
        Player player = client.player;
        if (!StunSlamData.isEnabled() || player == null || client.screen != null) {
            reset();
            return;
        }

        if (StunSlamData.getMode() == StunSlamData.Mode.SWAP_ASSIST) {
            tickSwapAssist(player);
        } else {
            tickMacro(client, player);
        }

        drainClicks(client);
    }

    /** Called when the macro keybind is pressed. */
    public static void startMacro(Minecraft client) {
        Player player = client.player;
        if (!StunSlamData.isEnabled() || player == null) return;
        if (StunSlamData.getMode() != StunSlamData.Mode.MACRO) return;
        if (macroStep >= 0) return; // already running

        Inventory inventory = player.getInventory();
        macroSpearSlot = findHotbarSlot(inventory, Weapon.SPEAR);
        macroAxeSlot   = findHotbarSlot(inventory, Weapon.AXE);
        macroMaceSlot  = findHotbarSlot(inventory, Weapon.MACE);

        // Nothing to run without the full set — a partial combo would just swing
        // whatever happened to be selected.
        if (macroSpearSlot < 0 || macroAxeSlot < 0 || macroMaceSlot < 0) return;

        macroStep = 0;
    }

    private static void tickSwapAssist(Player player) {
        Weapon held = classify(player.getMainHandItem());
        if (held == lastHeld) return;

        if (lastHeld == Weapon.SPEAR && held == Weapon.AXE) {
            pendingClicks += StunSlamData.getAxeClicks();
        } else if (lastHeld == Weapon.AXE && held == Weapon.MACE) {
            pendingClicks += StunSlamData.getMaceClicks();
        }

        lastHeld = held;
    }

    private static void tickMacro(Minecraft client, Player player) {
        if (macroStep < 0) return;

        Inventory inventory = player.getInventory();
        switch (macroStep) {
            case 0 -> { inventory.setSelectedSlot(macroSpearSlot); pendingClicks += 1; }
            case 1 -> { inventory.setSelectedSlot(macroAxeSlot);   pendingClicks += StunSlamData.getAxeClicks(); }
            case 2 -> { inventory.setSelectedSlot(macroMaceSlot);  pendingClicks += StunSlamData.getMaceClicks(); }
            default -> { macroStep = -1; return; }
        }
        macroStep++;

        // Keep swap assist from double-firing on the slots the macro just changed.
        lastHeld = classify(player.getMainHandItem());
    }

    private static void drainClicks(Minecraft client) {
        if (pendingClicks <= 0) return;
        pendingClicks--;
        ((MinecraftAutoclickerInvoker) (Object) client).superawesomemod$startAttack();
    }

    private static void reset() {
        lastHeld = Weapon.NONE;
        pendingClicks = 0;
        macroStep = -1;
    }

    private static Weapon classify(ItemStack stack) {
        if (stack.isEmpty()) return Weapon.NONE;
        if (stack.is(Items.MACE)) return Weapon.MACE;
        if (stack.is(ItemTags.SPEARS)) return Weapon.SPEAR;
        if (stack.is(ItemTags.AXES)) return Weapon.AXE;
        return Weapon.NONE;
    }

    private static int findHotbarSlot(Inventory inventory, Weapon weapon) {
        for (int i = 0; i < Inventory.getSelectionSize(); i++) {
            if (classify(inventory.getItem(i)) == weapon) return i;
        }
        return -1;
    }
}
