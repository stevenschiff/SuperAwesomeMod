package com.steveplays.superawesomemod;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;

public final class MiniMapOverlay {

    private MiniMapOverlay() {}

    private static final int BORDER_COLOR = 0xFF333333;
    private static final int BG_COLOR = 0xFF000000;

    public static void register() {
        HudRenderCallback.EVENT.register(MiniMapOverlay::onHudRender);
    }

    private static void onHudRender(GuiGraphics graphics, DeltaTracker tickCounter) {
        if (!MiniMapData.isEnabled()) return;
        if (!MiniMapData.isHudVisible()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) return;
        if (mc.player == null || mc.level == null) return;
        // Don't draw when full-screen map is open
        if (mc.screen instanceof MiniMapFullScreen) return;

        int size = MiniMapData.getMinimapSize();
        int guiW = graphics.guiWidth();
        int guiH = graphics.guiHeight();

        // Calculate origin based on corner setting
        int ox, oy;
        int margin = 4;
        switch (MiniMapData.getCorner()) {
            case 0 -> { ox = margin; oy = margin; } // top-left
            case 1 -> { ox = guiW - size - margin; oy = margin; } // top-right
            case 2 -> { ox = margin; oy = guiH - size - margin; } // bottom-left
            case 3 -> { ox = guiW - size - margin; oy = guiH - size - margin; } // bottom-right
            default -> { ox = guiW - size - margin; oy = margin; }
        }

        // Background
        graphics.fill(ox, oy, ox + size, oy + size, BG_COLOR);

        // Render terrain
        LocalPlayer player = mc.player;
        double playerX = player.getX();
        double playerZ = player.getZ();

        // Each pixel represents ~1 block at default scale
        float blocksPerPixel = 1.0f;
        int halfSize = size / 2;

        for (int px = 0; px < size; px++) {
            for (int py = 0; py < size; py++) {
                double worldX = playerX + (px - halfSize) * blocksPerPixel;
                double worldZ = playerZ + (py - halfSize) * blocksPerPixel;

                int chunkX = (int) Math.floor(worldX) >> 4;
                int chunkZ = (int) Math.floor(worldZ) >> 4;

                int[] colors = MiniMapChunkCache.get(chunkX, chunkZ);
                if (colors != null) {
                    int lx = ((int) Math.floor(worldX)) & 15;
                    int lz = ((int) Math.floor(worldZ)) & 15;
                    int color = colors[lx * 16 + lz];
                    if (color != 0xFF000000) {
                        graphics.fill(ox + px, oy + py, ox + px + 1, oy + py + 1, color);
                    }
                }
            }
        }

        // Draw waypoint markers
        for (MiniMapWaypoint wp : MiniMapData.getWaypoints()) {
            int wpPx = halfSize + (int) (wp.x() - playerX);
            int wpPy = halfSize + (int) (wp.z() - playerZ);
            if (wpPx >= 0 && wpPx < size && wpPy >= 0 && wpPy < size) {
                // Draw a 3x3 colored diamond
                int c = wp.color() | 0xFF000000;
                graphics.fill(ox + wpPx - 1, oy + wpPy, ox + wpPx + 2, oy + wpPy + 1, c);
                graphics.fill(ox + wpPx, oy + wpPy - 1, ox + wpPx + 1, oy + wpPy + 2, c);
            }
        }

        // Draw other players as white dots
        ClientLevel level = mc.level;
        for (Player p : level.players()) {
            if (p == player) continue;
            int pPx = halfSize + (int) (p.getX() - playerX);
            int pPy = halfSize + (int) (p.getZ() - playerZ);
            if (pPx >= 1 && pPx < size - 1 && pPy >= 1 && pPy < size - 1) {
                graphics.fill(ox + pPx - 1, oy + pPy - 1, ox + pPx + 2, oy + pPy + 2, 0xFFFFFFFF);
            }
        }

        // Draw self marker (center dot + direction indicator)
        int cx = ox + halfSize;
        int cy = oy + halfSize;
        graphics.fill(cx - 1, cy - 1, cx + 2, cy + 2, 0xFF00FF00);

        // Direction arrow based on player yaw
        float yawRad = (float) Math.toRadians(player.getYRot());
        int arrowLen = 4;
        int ax = (int) (-Math.sin(yawRad) * arrowLen);
        int ay = (int) (Math.cos(yawRad) * arrowLen);
        // Draw a small line for direction
        drawLine(graphics, cx, cy, cx + ax, cy + ay, 0xFF00FF00);

        // Border (2px)
        graphics.fill(ox, oy, ox + size, oy + 1, BORDER_COLOR);
        graphics.fill(ox, oy + size - 1, ox + size, oy + size, BORDER_COLOR);
        graphics.fill(ox, oy, ox + 1, oy + size, BORDER_COLOR);
        graphics.fill(ox + size - 1, oy, ox + size, oy + size, BORDER_COLOR);

        // Cardinal labels
        graphics.drawString(mc.font, "N", cx - 2, oy + 2, 0xFFFFFF, false);
        graphics.drawString(mc.font, "S", cx - 2, oy + size - 10, 0xFFFFFF, false);
        graphics.drawString(mc.font, "W", ox + 2, cy - 4, 0xFFFFFF, false);
        graphics.drawString(mc.font, "E", ox + size - 8, cy - 4, 0xFFFFFF, false);
    }

    private static void drawLine(GuiGraphics graphics, int x0, int y0, int x1, int y1, int color) {
        // Bresenham's line algorithm (simple version)
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            graphics.fill(x0, y0, x0 + 1, y0 + 1, color);
            if (x0 == x1 && y0 == y1) break;
            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; x0 += sx; }
            if (e2 < dx) { err += dx; y0 += sy; }
        }
    }
}
