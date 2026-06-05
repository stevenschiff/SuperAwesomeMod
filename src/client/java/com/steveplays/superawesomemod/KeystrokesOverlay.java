package com.steveplays.superawesomemod;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class KeystrokesOverlay {

    private static final int KEY_SIZE = 22;
    private static final int GAP     = 2;

    // Colors
    private static final int BG_PRESSED   = 0xC0CCCCCC; // slightly darker white
    private static final int BG_RELEASED  = 0xC0000000; // black
    private static final int TEXT_COLOR   = 0xFFFFFFFF;  // white text

    private KeystrokesOverlay() {}

    public static void register() {
        HudRenderCallback.EVENT.register(KeystrokesOverlay::onHudRender);
    }

    private static void onHudRender(GuiGraphics graphics, DeltaTracker tickCounter) {
        if (!KeystrokesData.isEnabled()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null) return;

        int width = graphics.guiWidth();

        // Top-right corner with some padding
        int baseX = width - 3 * (KEY_SIZE + GAP) - 6;
        int baseY = 6;

        boolean w     = mc.options.keyUp.isDown();
        boolean a     = mc.options.keyLeft.isDown();
        boolean s     = mc.options.keyDown.isDown();
        boolean d     = mc.options.keyRight.isDown();
        boolean space = mc.options.keyJump.isDown();

        // Row 0: W (centered)
        drawKey(graphics, mc, "W", baseX + KEY_SIZE + GAP, baseY, w);

        // Row 1: A S D
        int row1Y = baseY + KEY_SIZE + GAP;
        drawKey(graphics, mc, "A", baseX, row1Y, a);
        drawKey(graphics, mc, "S", baseX + KEY_SIZE + GAP, row1Y, s);
        drawKey(graphics, mc, "D", baseX + 2 * (KEY_SIZE + GAP), row1Y, d);

        // Row 2: Spacebar (full width)
        int row2Y = row1Y + KEY_SIZE + GAP;
        int spaceWidth = 3 * KEY_SIZE + 2 * GAP;
        int bg = space ? BG_PRESSED : BG_RELEASED;
        graphics.fill(baseX, row2Y, baseX + spaceWidth, row2Y + KEY_SIZE, bg);
        String spaceLabel = "\u2014"; // em dash to represent spacebar
        int textW = mc.font.width(spaceLabel);
        graphics.drawString(mc.font, spaceLabel,
            baseX + (spaceWidth - textW) / 2,
            row2Y + (KEY_SIZE - 8) / 2,
            TEXT_COLOR, true);
    }

    private static void drawKey(GuiGraphics graphics, Minecraft mc,
                                String label, int x, int y, boolean pressed) {
        int bg = pressed ? BG_PRESSED : BG_RELEASED;
        graphics.fill(x, y, x + KEY_SIZE, y + KEY_SIZE, bg);
        int textW = mc.font.width(label);
        graphics.drawString(mc.font, label,
            x + (KEY_SIZE - textW) / 2,
            y + (KEY_SIZE - 8) / 2,
            TEXT_COLOR, true);
    }
}
