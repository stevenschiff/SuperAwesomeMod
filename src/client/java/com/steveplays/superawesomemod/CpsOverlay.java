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

        String text = leftCps + " | " + rightCps + " CPS";

        float scale = CpsData.getScale() / 5.0f; // scale 5 = 1.0x

        // Position: right of the hotbar. Hotbar right edge is at width/2 + 91.
        int hotbarRight = width / 2 + 91;
        int textWidth = mc.font.width(text);

        graphics.pose().pushMatrix();
        // Anchor point: right of hotbar, vertically centered on hotbar row.
        float anchorX = hotbarRight + 8;
        float anchorY = height - 14;
        graphics.pose().translate(anchorX, anchorY);
        graphics.pose().scale(scale, scale);

        // Background box
        int bgWidth  = textWidth + 6;
        int bgHeight = 12;
        graphics.fill(-3, -2, bgWidth - 3, bgHeight - 2, 0x80000000);

        // Text
        graphics.drawString(mc.font, text, 0, 0, 0xFFFFFF, true);

        graphics.pose().popMatrix();
    }
}
