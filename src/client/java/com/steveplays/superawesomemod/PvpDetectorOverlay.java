package com.steveplays.superawesomemod;

import java.util.List;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * Top-right HUD listing tracked players and their suspicion stats.
 *
 * Layout per row:  [name]  [ping]  H:[hits]  Max:[reach]  [BAND]
 * Suspicion band is colored:  NONE=gray  LOW=yellow  MED=orange  HIGH=red
 */
@SuppressWarnings("deprecation")
public final class PvpDetectorOverlay {

    private static final int ROW_HEIGHT = 11;
    private static final int PADDING    = 4;
    private static final int MAX_ROWS   = 6;

    private static final int COL_NONE = 0xFF888888;
    private static final int COL_LOW  = 0xFFE0C040;
    private static final int COL_MED  = 0xFFE08020;
    private static final int COL_HIGH = 0xFFE03030;
    private static final int COL_TXT  = 0xFFE0E0E0;
    private static final int COL_BG   = 0x80000000;
    private static final int COL_PING_OK   = 0xFF60D060;
    private static final int COL_PING_WARN = 0xFFE0C040;
    private static final int COL_PING_BAD  = 0xFFE03030;

    private PvpDetectorOverlay() {}

    public static void register() {
        HudRenderCallback.EVENT.register(PvpDetectorOverlay::onHudRender);
    }

    private static void onHudRender(GuiGraphics g, DeltaTracker tickCounter) {
        if (!PvpDetectorData.isEnabled()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) return;

        PvpDetectorTracker.tick();
        List<PvpDetectorTracker.PlayerStats> rows = PvpDetectorTracker.snapshot();
        if (rows.isEmpty()) {
            drawIdle(g, mc.font);
            return;
        }
        if (rows.size() > MAX_ROWS) rows = rows.subList(0, MAX_ROWS);

        Font font = mc.font;
        int titleW = font.width("PvP Watch");
        int width  = Math.max(titleW, longestRowWidth(font, rows)) + PADDING * 2;
        int height = (rows.size() + 1) * ROW_HEIGHT + PADDING * 2;

        int x = g.guiWidth() - width - PADDING;
        int y = PADDING;

        // Background panel.
        g.fill(x, y, x + width, y + height, COL_BG);

        // Title row.
        g.drawString(font, Component.literal("PvP Watch"), x + PADDING, y + PADDING, COL_TXT, false);

        int rowY = y + PADDING + ROW_HEIGHT;
        for (PvpDetectorTracker.PlayerStats st : rows) {
            String row = formatRow(st);
            int bandColor = bandColor(st.band());
            g.drawString(font, Component.literal(row), x + PADDING, rowY, COL_TXT, false);

            // Re-draw the trailing band token in band-color so it pops.
            String badge = " " + st.band().name();
            int badgeX = x + PADDING + font.width(row) - font.width(badge);
            g.drawString(font, Component.literal(badge), badgeX, rowY, bandColor, false);

            // Recolor the ping field.
            int pingColor = pingColor(st.pingMs);
            String pingTok = " " + (st.pingMs < 0 ? "?" : st.pingMs) + "ms";
            int pingX = x + PADDING + font.width(st.name) + font.width(" ");
            g.drawString(font, Component.literal(pingTok.trim()), pingX, rowY, pingColor, false);

            rowY += ROW_HEIGHT;
        }
    }

    private static String formatRow(PvpDetectorTracker.PlayerStats st) {
        String ping = st.pingMs < 0 ? "?ms" : (st.pingMs + "ms");
        return String.format("%s %s H:%d Max:%.1f %s",
            st.name,
            ping,
            st.hitCount(),
            st.maxReach(),
            st.band().name());
    }

    private static int longestRowWidth(Font font, List<PvpDetectorTracker.PlayerStats> rows) {
        int max = 0;
        for (PvpDetectorTracker.PlayerStats st : rows) {
            int w = font.width(formatRow(st));
            if (w > max) max = w;
        }
        return max;
    }

    private static int bandColor(PvpDetectorTracker.Band band) {
        return switch (band) {
            case HIGH -> COL_HIGH;
            case MED  -> COL_MED;
            case LOW  -> COL_LOW;
            case NONE -> COL_NONE;
        };
    }

    private static int pingColor(int ms) {
        if (ms < 0)    return COL_NONE;
        if (ms < 100)  return COL_PING_OK;
        if (ms < 250)  return COL_PING_WARN;
        return COL_PING_BAD;
    }

    private static void drawIdle(GuiGraphics g, Font font) {
        int width  = font.width("PvP Watch — no observed hits") + PADDING * 2;
        int height = ROW_HEIGHT + PADDING * 2;
        int x = g.guiWidth() - width - PADDING;
        int y = PADDING;
        g.fill(x, y, x + width, y + height, COL_BG);
        g.drawString(font,
            Component.literal("PvP Watch — no observed hits"),
            x + PADDING, y + PADDING, COL_NONE, false);
    }
}
