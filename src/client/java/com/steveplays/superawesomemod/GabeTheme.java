package com.steveplays.superawesomemod;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Palette and drawing helpers for the client menu.
 *
 * <p>Minecraft's GUI layer only fills axis-aligned rectangles, so the rounded panels
 * are chamfered: each corner steps inward a pixel per row. At a radius of three or
 * four that reads as a rounded corner and costs a handful of fills, where a real
 * arc would need a texture or a custom render pipeline.
 */
public final class GabeTheme {

    public static final String CLIENT_NAME = "Gabe Client";

    // Panels
    public static final int PANEL        = 0xF2232327;
    public static final int PANEL_HEADER = 0xF21A1A1E;
    public static final int ROW          = 0x00000000;
    public static final int ROW_HOVER    = 0x18FFFFFF;
    public static final int DIVIDER      = 0x22FFFFFF;

    // Accent — what an enabled row fades to.
    public static final int ACCENT       = 0xFF8B5CF6;
    public static final int ACCENT_SOFT  = 0xFF6D3FD4;
    public static final int ACCENT_TEXT  = 0xFFB794FF;

    // Text
    public static final int TEXT         = 0xFFFFFFFF;
    public static final int TEXT_DIM     = 0xFF9A9AA2;
    public static final int TEXT_FAINT   = 0xFF6A6A72;

    private GabeTheme() {}

    /** Chamfered rectangle — the stand-in for a rounded panel. */
    public static void roundedRect(GuiGraphics g, int x, int y, int w, int h, int r, int color) {
        if (w <= 0 || h <= 0) return;
        r = Math.min(r, Math.min(w, h) / 2);

        for (int i = 0; i < r; i++) {
            int inset = r - i;
            g.fill(x + inset, y + i, x + w - inset, y + i + 1, color);
            g.fill(x + inset, y + h - i - 1, x + w - inset, y + h - i, color);
        }
        g.fill(x, y + r, x + w, y + h - r, color);
    }

    /** Mixes two ARGB colours; t = 0 gives a, t = 1 gives b. */
    public static int blend(int a, int b, float t) {
        t = Math.clamp(t, 0f, 1f);
        int aa = (a >>> 24) & 0xFF, ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int ba = (b >>> 24) & 0xFF, br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        int oa = Math.round(aa + (ba - aa) * t);
        int or = Math.round(ar + (br - ar) * t);
        int og = Math.round(ag + (bg - ag) * t);
        int ob = Math.round(ab + (bb - ab) * t);
        return (oa << 24) | (or << 16) | (og << 8) | ob;
    }

    /**
     * Frame-rate independent ease toward a target. {@code speed} is roughly how much
     * of the remaining distance is covered per second, so the animation takes the same
     * wall-clock time at 30fps and at 240.
     */
    public static float approach(float current, float target, float dtSeconds, float speed) {
        float factor = 1f - (float) Math.exp(-speed * dtSeconds);
        return current + (target - current) * Math.clamp(factor, 0f, 1f);
    }
}
