package com.steveplays.superawesomemod;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.food.FoodData;

/**
 * Renders an AppleSkin-style saturation overlay on the vanilla hunger bar.
 * Each food icon gets a gold-tinted highlight proportional to how much
 * saturation is currently backing that slice of the hunger meter.
 */
@SuppressWarnings("deprecation")
public final class AppleSkinOverlay {

    // Hunger icon geometry (matches vanilla Gui.renderHunger)
    private static final int ICON_W = 9;
    private static final int ICON_GAP = 8;
    private static final int RIGHT_OFFSET = 91;
    private static final int Y_FROM_BOTTOM = 39;

    // Translucent saturation overlay color (gold). Top byte is alpha.
    private static final int OVERLAY_COLOR = 0xC0FFC840;

    private AppleSkinOverlay() {}

    public static void register() {
        HudRenderCallback.EVENT.register(AppleSkinOverlay::onHudRender);
    }

    private static void onHudRender(GuiGraphics g, DeltaTracker tickCounter) {
        if (!AppleSkinData.isEnabled()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) return;

        LocalPlayer p = mc.player;
        if (p == null) return;
        // Vanilla hides the hunger bar in creative/spectator — match that.
        if (p.isCreative() || p.isSpectator()) return;

        FoodData fd = p.getFoodData();
        float sat = fd.getSaturationLevel();
        if (sat <= 0f) return;

        int guiW = g.guiWidth();
        int guiH = g.guiHeight();
        int top = guiH - Y_FROM_BOTTOM;
        int rightEdge = guiW / 2 + RIGHT_OFFSET;

        for (int i = 0; i < 10; i++) {
            float satOverIcon = (float) Math.clamp(sat - i * 2f, 0f, 2f);
            if (satOverIcon <= 0f) break;

            int iconX = rightEdge - i * ICON_GAP - ICON_W;
            // Width 0..9 px, right-aligned within the icon to match vanilla's
            // half-icon convention (the empty side of a half-icon is the left).
            int fillW = Math.round(satOverIcon * (ICON_W / 2f));
            int x0 = iconX + ICON_W - fillW;

            // 7-pixel-tall band, inset by 1px top/bottom so the icon outline
            // still reads through.
            g.fill(x0, top + 1, iconX + ICON_W, top + 8, OVERLAY_COLOR);
        }
    }
}
