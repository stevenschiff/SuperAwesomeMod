package com.steveplays.superawesomemod;

import com.steveplays.superawesomemod.mixin.MinecraftAutoclickerInvoker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;

/**
 * Eats for you when hunger drops, then puts you back on the slot you were holding.
 *
 * <p>Hunger itself is server state we cannot write — but the server <em>tells</em> us
 * our food level in {@code ClientboundSetHealthPacket}, and reading it is all that is
 * needed to decide. Eating is then an ordinary item use, which every server accepts,
 * so this reaches the goal on servers where cancelling exhaustion cannot.
 *
 * <p>Only eats while standing still and not otherwise using an item, so it can't
 * interrupt a bow draw or fire mid-fight when you meant to swing.
 */
public final class AutoEatHandler {

    private static boolean eating = false;
    private static int previousSlot = -1;

    private AutoEatHandler() {}

    public static void tick(Minecraft client) {
        LocalPlayer player = client.player;
        if (!AutoEatData.isEnabled() || player == null || client.screen != null) {
            stop(client);
            return;
        }

        Inventory inventory = player.getInventory();

        if (eating) {
            // Keep holding use until the bite completes or the food leaves our hand.
            if (player.isUsingItem() && isFood(player.getMainHandItem())
                && player.getFoodData().getFoodLevel() < 20) {
                ((MinecraftAutoclickerInvoker) (Object) client).superawesomemod$startUseItem();
                return;
            }
            stop(client);
            return;
        }

        if (player.getFoodData().getFoodLevel() > AutoEatData.getThreshold()) return;
        // Don't hijack a bow draw, a shield, or anything else already in progress.
        if (player.isUsingItem()) return;

        int slot = bestFoodSlot(inventory);
        if (slot < 0) return;

        previousSlot = inventory.getSelectedSlot();
        inventory.setSelectedSlot(slot);
        eating = true;
        ((MinecraftAutoclickerInvoker) (Object) client).superawesomemod$startUseItem();
    }

    private static void stop(Minecraft client) {
        if (!eating) return;
        eating = false;

        LocalPlayer player = client.player;
        if (player != null && previousSlot >= 0) {
            player.getInventory().setSelectedSlot(previousSlot);
        }
        previousSlot = -1;
    }

    /** Highest-nutrition food in the hotbar, or -1. */
    private static int bestFoodSlot(Inventory inventory) {
        int best = -1;
        int bestNutrition = -1;

        for (int i = 0; i < Inventory.getSelectionSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            FoodProperties food = stack.get(DataComponents.FOOD);
            if (food == null) continue;

            if (food.nutrition() > bestNutrition) {
                bestNutrition = food.nutrition();
                best = i;
            }
        }
        return best;
    }

    private static boolean isFood(ItemStack stack) {
        return !stack.isEmpty() && stack.get(DataComponents.FOOD) != null;
    }
}
