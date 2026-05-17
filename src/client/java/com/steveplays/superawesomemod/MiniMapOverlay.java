package com.steveplays.superawesomemod;

import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public final class MiniMapOverlay {

    private MiniMapOverlay() {}

    private static final int BORDER_COLOR = 0xFF333333;

    // Texture-based rendering for performance
    private static DynamicTexture mapTexture;
    private static NativeImage mapImage;
    private static Identifier mapTextureId;
    private static int currentTextureSize = 0;

    // Update throttling: only regenerate terrain every N ticks
    private static int tickCounter = 0;
    private static final int UPDATE_INTERVAL = 5; // ~4 times per second
    private static double lastPlayerX = Double.NaN;
    private static double lastPlayerZ = Double.NaN;
    private static boolean needsUpdate = true;

    // Cached entity positions to avoid iterating all entities every frame
    private record EntityPos(double x, double z, int color) {}
    private static List<EntityPos> cachedPlayers = List.of();
    private static List<EntityPos> cachedEntities = List.of();

    public static void register() {
        HudRenderCallback.EVENT.register(MiniMapOverlay::onHudRender);
    }

    private static void ensureTexture(int size) {
        if (mapImage != null && currentTextureSize == size) return;

        // Clean up old texture
        if (mapTexture != null) {
            mapTexture.close();
        }
        if (mapTextureId != null) {
            Minecraft.getInstance().getTextureManager().release(mapTextureId);
            mapTextureId = null;
        }

        // Create new — use size constructor so getPixels() returns the internal image
        mapTexture = new DynamicTexture(() -> "superawesomemod_minimap_hud", size, size, false);
        mapImage = mapTexture.getPixels();
        mapTextureId = Identifier.fromNamespaceAndPath("superawesomemod", "minimap_hud");
        Minecraft.getInstance().getTextureManager().register(mapTextureId, mapTexture);
        currentTextureSize = size;
        needsUpdate = true;
    }

    private static void regenerateImage(Minecraft mc, int size) {
        LocalPlayer player = mc.player;
        if (player == null) return;

        double playerX = player.getX();
        double playerZ = player.getZ();
        int halfSize = size / 2;
        int startBX = (int) Math.floor(playerX) - halfSize;
        int startBZ = (int) Math.floor(playerZ) - halfSize;

        for (int px = 0; px < size; px++) {
            int bx = startBX + px;
            int chunkX = bx >> 4;
            int lx = bx & 15;

            // Cache chunk lookup per column — only changes when chunkZ changes
            int lastChunkZ = Integer.MIN_VALUE;
            int[] colors = null;

            for (int py = 0; py < size; py++) {
                int bz = startBZ + py;
                int cz = bz >> 4;

                if (cz != lastChunkZ) {
                    colors = MiniMapChunkCache.get(chunkX, cz);
                    lastChunkZ = cz;
                }

                int color;
                if (colors != null) {
                    color = colors[lx * 16 + (bz & 15)];
                } else {
                    color = 0xFF000000;
                }
                mapImage.setPixelABGR(px, py, argbToAbgr(color));
            }
        }

        lastPlayerX = playerX;
        lastPlayerZ = playerZ;
    }

    private static int argbToAbgr(int argb) {
        int a = (argb >> 24) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        return (a << 24) | (b << 16) | (g << 8) | r;
    }

    private static void onHudRender(GuiGraphics graphics, DeltaTracker tickCounter_) {
        if (!MiniMapData.isEnabled()) return;
        if (!MiniMapData.isHudVisible()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) return;
        if (mc.player == null || mc.level == null) return;
        if (mc.screen instanceof MiniMapFullScreen) return;

        int size = MiniMapData.getMinimapSize();
        ensureTexture(size);

        int guiW = graphics.guiWidth();
        int guiH = graphics.guiHeight();

        // Calculate origin based on corner setting
        int ox, oy;
        int margin = 4;
        switch (MiniMapData.getCorner()) {
            case 0 -> { ox = margin; oy = margin; }
            case 1 -> { ox = guiW - size - margin; oy = margin; }
            case 2 -> { ox = margin; oy = guiH - size - margin; }
            case 3 -> { ox = guiW - size - margin; oy = guiH - size - margin; }
            default -> { ox = guiW - size - margin; oy = margin; }
        }

        // Check if terrain texture needs regenerating
        tickCounter++;
        double playerX = mc.player.getX();
        double playerZ = mc.player.getZ();
        boolean playerMoved = Double.isNaN(lastPlayerX)
            || Math.abs(playerX - lastPlayerX) > 1
            || Math.abs(playerZ - lastPlayerZ) > 1;

        if (needsUpdate || (tickCounter >= UPDATE_INTERVAL && playerMoved)) {
            regenerateImage(mc, size);
            mapTexture.upload();
            tickCounter = 0;
            needsUpdate = false;

            // Refresh entity cache at the same interval as terrain
            refreshEntityCache(mc);
        }

        // Render terrain as single texture blit
        graphics.blit(RenderPipelines.GUI_TEXTURED, mapTextureId, ox, oy, 0.0f, 0.0f, size, size, size, size);

        // Draw waypoint markers
        LocalPlayer player = mc.player;
        int halfSize = size / 2;

        for (MiniMapWaypoint wp : MiniMapData.getVisibleWaypoints()) {
            int wpPx = halfSize + (int) (wp.x() - playerX);
            int wpPy = halfSize + (int) (wp.z() - playerZ);
            if (wpPx >= 0 && wpPx < size && wpPy >= 0 && wpPy < size) {
                int c = wp.color() | 0xFF000000;
                // Diamond shape
                graphics.fill(ox + wpPx - 2, oy + wpPy, ox + wpPx + 3, oy + wpPy + 1, c);
                graphics.fill(ox + wpPx - 1, oy + wpPy - 1, ox + wpPx + 2, oy + wpPy + 2, c);
                graphics.fill(ox + wpPx, oy + wpPy - 2, ox + wpPx + 1, oy + wpPy + 3, c);
            }
        }

        // Draw entities from cache (refreshed at UPDATE_INTERVAL, not every frame)
        int entityMode = MiniMapData.getEntityMode();

        if (entityMode >= 1) {
            // Draw players (cyan diamond shape)
            for (EntityPos ep : cachedPlayers) {
                int pPx = halfSize + (int) (ep.x - playerX);
                int pPy = halfSize + (int) (ep.z - playerZ);
                if (pPx >= 2 && pPx < size - 2 && pPy >= 2 && pPy < size - 2) {
                    graphics.fill(ox + pPx - 1, oy + pPy, ox + pPx + 2, oy + pPy + 1, 0xFF00FFFF);
                    graphics.fill(ox + pPx, oy + pPy - 1, ox + pPx + 1, oy + pPy + 2, 0xFF00FFFF);
                    graphics.fill(ox + pPx - 2, oy + pPy, ox + pPx - 1, oy + pPy + 1, 0xFFFFFFFF);
                    graphics.fill(ox + pPx + 2, oy + pPy, ox + pPx + 3, oy + pPy + 1, 0xFFFFFFFF);
                    graphics.fill(ox + pPx, oy + pPy - 2, ox + pPx + 1, oy + pPy - 1, 0xFFFFFFFF);
                    graphics.fill(ox + pPx, oy + pPy + 2, ox + pPx + 1, oy + pPy + 3, 0xFFFFFFFF);
                }
            }
        }

        if (entityMode >= 2) {
            // Draw other living entities (small orange dot)
            for (EntityPos ep : cachedEntities) {
                int ePx = halfSize + (int) (ep.x - playerX);
                int ePy = halfSize + (int) (ep.z - playerZ);
                if (ePx >= 0 && ePx < size && ePy >= 0 && ePy < size) {
                    graphics.fill(ox + ePx, oy + ePy, ox + ePx + 1, oy + ePy + 1, 0xFFFF8800);
                }
            }
        }

        // Self marker (center green dot with direction arrow)
        int cx = ox + halfSize;
        int cy = oy + halfSize;
        graphics.fill(cx - 1, cy - 1, cx + 2, cy + 2, 0xFF00FF00);

        // Direction arrow
        float yawRad = (float) Math.toRadians(player.getYRot());
        int arrowLen = 4;
        int ax = (int) (-Math.sin(yawRad) * arrowLen);
        int ay = (int) (Math.cos(yawRad) * arrowLen);
        drawLine(graphics, cx, cy, cx + ax, cy + ay, 0xFF00FF00);

        // Border
        graphics.fill(ox, oy, ox + size, oy + 1, BORDER_COLOR);
        graphics.fill(ox, oy + size - 1, ox + size, oy + size, BORDER_COLOR);
        graphics.fill(ox, oy, ox + 1, oy + size, BORDER_COLOR);
        graphics.fill(ox + size - 1, oy, ox + size, oy + size, BORDER_COLOR);

        // Cardinal labels
        graphics.drawString(mc.font, "N", cx - 2, oy + 2, 0xFFFFFF, false);
        graphics.drawString(mc.font, "S", cx - 2, oy + size - 10, 0xFFFFFF, false);
        graphics.drawString(mc.font, "W", ox + 2, cy - 4, 0xFFFFFF, false);
        graphics.drawString(mc.font, "E", ox + size - 8, cy - 4, 0xFFFFFF, false);

        // Player coordinates below the minimap
        String coordText = String.format("X: %d  Z: %d", (int) playerX, (int) playerZ);
        int textX = ox + size / 2 - mc.font.width(coordText) / 2;
        int textY = oy + size + 2;
        graphics.drawString(mc.font, coordText, textX, textY, 0xDDDDDD, false);
    }

    private static void refreshEntityCache(Minecraft mc) {
        ClientLevel level = mc.level;
        LocalPlayer player = mc.player;
        if (level == null || player == null) return;

        int entityMode = MiniMapData.getEntityMode();

        if (entityMode >= 1) {
            List<EntityPos> players = new ArrayList<>();
            for (Player p : level.players()) {
                if (p == player) continue;
                players.add(new EntityPos(p.getX(), p.getZ(), 0xFF00FFFF));
            }
            cachedPlayers = players;
        }

        if (entityMode >= 2) {
            List<EntityPos> entities = new ArrayList<>();
            for (Entity entity : level.entitiesForRendering()) {
                if (entity instanceof Player) continue;
                if (!(entity instanceof LivingEntity)) continue;
                entities.add(new EntityPos(entity.getX(), entity.getZ(), 0xFFFF8800));
            }
            cachedEntities = entities;
        }
    }

    private static void drawLine(GuiGraphics graphics, int x0, int y0, int x1, int y1, int color) {
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
