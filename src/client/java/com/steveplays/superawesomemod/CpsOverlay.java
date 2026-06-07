package com.steveplays.superawesomemod;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

@SuppressWarnings("deprecation")
public final class CpsOverlay {

    private CpsOverlay() {}

    public static void register() {
        HudRenderCallback.EVENT.register(CpsOverlay::onHudRender);
    }

    private static void onHudRender(GuiGraphics graphics, DeltaTracker tickCounter) {
        if (!CpsData.isEnabled()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null) return;

        int width  = graphics.guiWidth();
        int height = graphics.guiHeight();

        int leftCps  = CpsData.getLeftCps();
        int rightCps = CpsData.getRightCps();

        String leftText  = String.valueOf(leftCps);
        String separator = " | ";
        String rightText = String.valueOf(rightCps);
        String cpsLabel  = " CPS";
        String fullText  = leftText + separator + rightText + cpsLabel;

        float scale = CpsData.getScale() / 5.0f; // scale 5 = 1.0x

        // Position: right of the hotbar. Hotbar right edge is at width/2 + 91.
        int hotbarRight = width / 2 + 91;
        int fullTextWidth = mc.font.width(fullText);

        // Compute box dimensions in scaled space
        int padding = 4;
        int boxWidth  = fullTextWidth + padding * 2;
        int boxHeight = mc.font.lineHeight + padding * 2;

        // Anchor: right of hotbar, vertically centered on hotbar row
        int anchorX = (int)(hotbarRight + CpsData.getOffset());
        int anchorY = (int)(height - 10 - (boxHeight * scale) / 2);

        graphics.pose().pushMatrix();
        graphics.pose().translate((float) anchorX, (float) anchorY);
        graphics.pose().scale(scale, scale);

        // Pure black background
        graphics.fill(0, 0, boxWidth, boxHeight, 0xFF000000);

        // Draw text centered in box
        int textX = padding;
        int textY = padding;
        graphics.drawString(mc.font, fullText, textX, textY, 0xFFFFFFFF, false);

        graphics.pose().popMatrix();
    }
}
