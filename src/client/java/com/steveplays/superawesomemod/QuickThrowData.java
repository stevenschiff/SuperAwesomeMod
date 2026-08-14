package com.steveplays.superawesomemod;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class QuickThrowData {

    private static boolean enabled = false;

    private QuickThrowData() {}

    public static boolean isEnabled()           { return enabled; }
    public static void    setEnabled(boolean e) { enabled = e; }

    /**
     * Items whose throw or eat rate is worth freeing from the client's own cooldown.
     * Deliberately a list rather than "everything": clearing the delay for blocks and
     * tools is Fast Actions' job, and doing it here too would make that toggle a lie.
     */
    public static boolean isQuickItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.is(Items.ENDER_PEARL)
            || stack.is(Items.WIND_CHARGE)
            || stack.is(Items.SNOWBALL)
            || stack.is(Items.EGG)
            || stack.is(Items.SPLASH_POTION)
            || stack.is(Items.LINGERING_POTION)
            || stack.is(Items.EXPERIENCE_BOTTLE)
            || stack.is(Items.FIREWORK_ROCKET)
            || stack.is(Items.GOLDEN_APPLE)
            || stack.is(Items.ENCHANTED_GOLDEN_APPLE);
    }
}
