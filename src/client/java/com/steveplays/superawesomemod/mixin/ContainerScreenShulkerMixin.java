package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.ShulkerTooltipData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(AbstractContainerScreen.class)
public abstract class ContainerScreenShulkerMixin {

    @Shadow protected Slot hoveredSlot;

    @Unique private static final int SLOT_SIZE    = 18;
    @Unique private static final int COLS         = 9;
    @Unique private static final int ROWS         = 3;
    @Unique private static final int PADDING      = 7;
    @Unique private static final int TITLE_HEIGHT = 12;

    /**
     * Returns true when the shulker preview should be active: feature enabled,
     * shift held, and hovering a shulker box item.
     */
    @Unique
    private boolean superawesomemod$shouldShowPreview() {
        if (!ShulkerTooltipData.isEnabled()) return false;
        long handle = GLFW.glfwGetCurrentContext();
        boolean shiftHeld = GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS
                         || GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
        if (!shiftHeld) return false;
        if (this.hoveredSlot == null || !this.hoveredSlot.hasItem()) return false;
        ItemStack stack = this.hoveredSlot.getItem();
        if (!(stack.getItem() instanceof BlockItem blockItem)) return false;
        return blockItem.getBlock() instanceof ShulkerBoxBlock;
    }

    /**
     * Suppress the vanilla tooltip when our shulker preview is showing.
     */
    @Inject(method = "renderTooltip", at = @At("HEAD"), cancellable = true)
    private void superawesomemod$hideVanillaTooltip(GuiGraphics graphics, int mouseX, int mouseY, CallbackInfo ci) {
        if (superawesomemod$shouldShowPreview()) {
            ci.cancel();
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void superawesomemod$renderShulkerPreview(
            GuiGraphics graphics, int mouseX, int mouseY, float partialTick,
            CallbackInfo ci) {

        if (!superawesomemod$shouldShowPreview()) return;

        ItemStack stack = this.hoveredSlot.getItem();

        // Read shulker contents via 1.21.x component API
        ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
        if (contents == null) return;

        // Collect items into a list for indexed access
        List<ItemStack> items = new ArrayList<>();
        contents.stream().forEach(items::add);
        if (items.isEmpty()) return;

        // Pad to 27 slots so grid positions are correct
        while (items.size() < 27) {
            items.add(ItemStack.EMPTY);
        }

        // Compute scale: scale 1 -> 0.5x, scale 10 -> 2.0x
        int scale = ShulkerTooltipData.getScale();
        float scaleFactor = 0.5f + (scale - 1) * (1.5f / 9.0f);

        // Tooltip dimensions in screen pixels
        int bgW = COLS * SLOT_SIZE + PADDING * 2;
        int bgH = ROWS * SLOT_SIZE + TITLE_HEIGHT + PADDING * 2;
        int totalW = (int) (bgW * scaleFactor);
        int totalH = (int) (bgH * scaleFactor);

        // Position near cursor with edge clamping
        Minecraft mc = Minecraft.getInstance();
        int screenW = graphics.guiWidth();
        int screenH = graphics.guiHeight();

        int tooltipX = mouseX + 12;
        int tooltipY = mouseY - 12;

        if (tooltipX + totalW > screenW) tooltipX = mouseX - totalW - 4;
        if (tooltipY + totalH > screenH) tooltipY = screenH - totalH;
        if (tooltipX < 0) tooltipX = 0;
        if (tooltipY < 0) tooltipY = 0;

        // Use the 1.21.11 Matrix3x2fStack API (pushMatrix/popMatrix, 2D translate/scale)
        graphics.pose().pushMatrix();
        graphics.pose().translate((float) tooltipX, (float) tooltipY);
        graphics.pose().scale(scaleFactor, scaleFactor);

        // Background
        graphics.fill(0, 0, bgW, bgH, 0xCC100010);

        // Border (1px, purple tint)
        int bc = 0xFF8040C0;
        graphics.fill(0, 0, bgW, 1, bc);
        graphics.fill(0, bgH - 1, bgW, bgH, bc);
        graphics.fill(0, 0, 1, bgH, bc);
        graphics.fill(bgW - 1, 0, bgW, bgH, bc);

        // Shulker box name
        Component name = stack.getHoverName();
        graphics.drawString(mc.font, name, PADDING, PADDING, 0xFFFFFF, true);

        // Item grid
        int gridX = PADDING;
        int gridY = PADDING + TITLE_HEIGHT;

        for (int i = 0; i < Math.min(items.size(), 27); i++) {
            ItemStack slotStack = items.get(i);
            if (slotStack.isEmpty()) continue;

            int col = i % COLS;
            int row = i / COLS;
            int ix = gridX + col * SLOT_SIZE + 1;
            int iy = gridY + row * SLOT_SIZE + 1;

            graphics.renderItem(slotStack, ix, iy);
            graphics.renderItemDecorations(mc.font, slotStack, ix, iy);
        }

        graphics.pose().popMatrix();
    }
}
