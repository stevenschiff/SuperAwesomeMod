package com.steveplays.superawesomemod;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Auto Totem: keeps a Totem of Undying in the offhand.
 *
 * <p>Uses one {@link ClickType#SWAP} click with button 40 — the offhand-swap button
 * the vanilla F key uses — so a refill is a single ordinary container click rather
 * than a pickup/place/put-back dance with a live cursor. Nothing here is a custom
 * packet; it's the same message the client sends when you press F on a slot.
 */
public final class AutoTotemHandler {

    /** Button number that {@code AbstractContainerMenu} treats as "swap with offhand". */
    private static final int OFFHAND_SWAP_BUTTON = 40;

    private static int cooldown = 0;

    private AutoTotemHandler() {}

    public static void tick(Minecraft client) {
        if (!AutoTotemData.isEnabled()) {
            cooldown = 0;
            return;
        }

        Player player = client.player;
        if (player == null || client.gameMode == null) return;

        // Only with no screen open: clicking container 0 while a chest is open would
        // desync the menu the server thinks we have in front of us.
        if (client.screen != null) return;

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        if (player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) return;

        int menuSlot = findTotemMenuSlot(player.getInventory());
        if (menuSlot < 0) return;

        client.gameMode.handleInventoryMouseClick(
            player.inventoryMenu.containerId, menuSlot,
            OFFHAND_SWAP_BUTTON, ClickType.SWAP, player);

        cooldown = AutoTotemData.getDelayTicks();
    }

    /**
     * Finds a totem and returns its slot number in the player's inventory menu, or -1.
     * The menu numbers the hotbar after the main inventory, so the two ranges convert
     * differently.
     */
    private static int findTotemMenuSlot(Inventory inventory) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.is(Items.TOTEM_OF_UNDYING)) continue;

            if (i < Inventory.getSelectionSize()) {
                return InventoryMenu.USE_ROW_SLOT_START + i;
            }
            if (i < InventoryMenu.INV_SLOT_END) {
                return i;
            }
        }
        return -1;
    }
}
