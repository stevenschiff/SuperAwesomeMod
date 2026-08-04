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

    private static final int BASE_ICON_SIZE = 16;
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
        int scale  = ArmorHudData.getScale();
        int iconSize = BASE_ICON_SIZE * scale;
        int gap = 2 * scale;

        // Calculate total row width and position it left of the hotbar
        int rowWidth = SLOTS.length * iconSize + (SLOTS.length - 1) * gap;
        int xLeft    = width / 2 - 91 - SHIELD_GAP - rowWidth;
        int yTop     = height - 19 - (iconSize - BASE_ICON_SIZE); // align bottom with hotbar

        for (int i = 0; i < SLOTS.length; i++) {
            ItemStack stack = player.getItemBySlot(SLOTS[i]);
            if (stack.isEmpty()) continue;

            int x = xLeft + i * (iconSize + gap);

            // Render the item icon scaled
            graphics.pose().pushMatrix();
            graphics.pose().translate((float) x, (float) yTop);
            graphics.pose().scale((float) scale, (float) scale);
            graphics.renderItem(stack, 0, 0);
            graphics.renderItemDecorations(mc.font, stack, 0, 0);
            graphics.pose().popMatrix();

            // Draw durability number above the icon
            if (stack.isDamageableItem()) {
                int durability = stack.getMaxDamage() - stack.getDamageValue();
                int maxDurability = stack.getMaxDamage();
                float ratio = (float) durability / maxDurability;

                // Color: green when full, yellow at half, red when low
                int color;
                if (ratio > 0.6f) {
                    color = 0x55FF55; // green
                } else if (ratio > 0.3f) {
                    color = 0xFFFF55; // yellow
                } else {
                    color = 0xFF5555; // red
                }

                String text = durability + "/" + maxDurability;
                int textWidth = mc.font.width(text);
                int textX = x + iconSize / 2 - textWidth / 2;
                int textY = yTop - 10;
                graphics.drawString(mc.font, text, textX, textY, color, true);
            }
        }
    }
}
