package com.steveplays.superawesomemod;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

@SuppressWarnings("deprecation")
public final class KeystrokesOverlay {

    private static final int BASE_KEY_SIZE = 22;
    private static final int BASE_GAP     = 2;

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

        int screenWidth  = graphics.guiWidth();
        int screenHeight = graphics.guiHeight();
        float scale = KeystrokesData.getScale() / 5.0f; // scale 5 = 1.0x

        int keySize = BASE_KEY_SIZE;
        int gap     = BASE_GAP;

        // Total widget size in unscaled coords
        int widgetW = 3 * keySize + 2 * gap;
        int widgetH = 3 * keySize + 2 * gap; // 3 rows: W, ASD, Space

        // Compute anchor based on corner
        int corner = KeystrokesData.getCorner();
        int padding = 6;
        float anchorX, anchorY;
        switch (corner) {
            case 0 -> { // Top-Left
                anchorX = padding;
                anchorY = padding;
            }
            case 2 -> { // Bottom-Left
                anchorX = padding;
                anchorY = screenHeight - widgetH * scale - padding;
            }
            case 3 -> { // Bottom-Right
                anchorX = screenWidth - widgetW * scale - padding;
                anchorY = screenHeight - widgetH * scale - padding;
            }
            default -> { // 1 = Top-Right
                anchorX = screenWidth - widgetW * scale - padding;
                anchorY = padding;
            }
        }

        boolean w     = mc.options.keyUp.isDown();
        boolean a     = mc.options.keyLeft.isDown();
        boolean s     = mc.options.keyDown.isDown();
        boolean d     = mc.options.keyRight.isDown();
        boolean space = mc.options.keyJump.isDown();

        graphics.pose().pushMatrix();
        graphics.pose().translate(anchorX, anchorY);
        graphics.pose().scale(scale, scale);

        // Row 0: W (centered)
        drawKey(graphics, mc, "W", keySize + gap, 0, keySize, w);

        // Row 1: A S D
        int row1Y = keySize + gap;
        drawKey(graphics, mc, "A", 0, row1Y, keySize, a);
        drawKey(graphics, mc, "S", keySize + gap, row1Y, keySize, s);
        drawKey(graphics, mc, "D", 2 * (keySize + gap), row1Y, keySize, d);

        // Row 2: Spacebar (full width)
        int row2Y = 2 * (keySize + gap);
        int spaceWidth = widgetW;
        int bg = space ? BG_PRESSED : BG_RELEASED;
        graphics.fill(0, row2Y, spaceWidth, row2Y + keySize, bg);
        String spaceLabel = "\u2014"; // em dash to represent spacebar
        int textW = mc.font.width(spaceLabel);
        graphics.drawString(mc.font, spaceLabel,
            (spaceWidth - textW) / 2,
            row2Y + (keySize - 8) / 2,
            TEXT_COLOR, true);

        graphics.pose().popMatrix();
    }

    private static void drawKey(GuiGraphics graphics, Minecraft mc,
                                String label, int x, int y, int size, boolean pressed) {
        int bg = pressed ? BG_PRESSED : BG_RELEASED;
        graphics.fill(x, y, x + size, y + size, bg);
        int textW = mc.font.width(label);
        graphics.drawString(mc.font, label,
            x + (size - textW) / 2,
            y + (size - 8) / 2,
            TEXT_COLOR, true);
    }
}
