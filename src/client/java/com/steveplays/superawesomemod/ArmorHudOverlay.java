package com.steveplays.superawesomemod;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

@SuppressWarnings("deprecation")
public final class ArmorHudOverlay {

    // Helmet, chestplate, leggings, boots — left to right.
    private static final EquipmentSlot[] SLOTS = {
        EquipmentSlot.HEAD,
        EquipmentSlot.CHEST,
        EquipmentSlot.LEGS,
        EquipmentSlot.FEET
    };

    private static final int ICON_SIZE = 16;
    // Pixels of empty space to leave between the armor row and the hotbar/offhand for the shield slot.
    private static final int SHIELD_GAP = 32;

    private ArmorHudOverlay() {}

    public static void register() {
        HudRenderCallback.EVENT.register(ArmorHudOverlay::onHudRender);
    }

    private static void onHudRender(GuiGraphics graphics, DeltaTracker tickCounter) {
        if (!ArmorHudData.isEnabled()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) return;

        LocalPlayer player = mc.player;
        if (player == null) return;

        int width  = graphics.guiWidth();
        int height = graphics.guiHeight();

        // Hotbar's left edge is at width/2 - 91. Place the armor row to the left of that,
        // leaving SHIELD_GAP pixels free for the offhand/shield slot.
        int rowWidth = SLOTS.length * ICON_SIZE;
        int xLeft    = width / 2 - 91 - SHIELD_GAP - rowWidth;
        int yTop     = height - 19; // matches the y of hotbar items

        for (int i = 0; i < SLOTS.length; i++) {
            ItemStack stack = player.getItemBySlot(SLOTS[i]);
            if (stack.isEmpty()) continue;

            int x = xLeft + i * ICON_SIZE;
            graphics.renderItem(stack, x, yTop);
            // Draws the vanilla durability bar (green/yellow/red) at the bottom of the icon.
            graphics.renderItemDecorations(mc.font, stack, x, yTop);
        }
    }
}
