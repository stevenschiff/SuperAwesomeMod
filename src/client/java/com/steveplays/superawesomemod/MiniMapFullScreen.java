package com.steveplays.superawesomemod;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class MiniMapFullScreen extends Screen {

    private double panX, panZ; // world coordinates of map center
    private float zoom = 1.0f; // pixels per block
    private boolean showLegend = false;

    private static final float MIN_ZOOM = 0.0001f; // effectively no cap
    private static final float MAX_ZOOM = 8.0f;
    private static final float PAN_SPEED = 10.0f;

    // Mouse drag tracking
    private boolean dragging = false;
    private double lastMouseX, lastMouseY;

    // Texture-based rendering for performance
    private static DynamicTexture fsTexture;
    private static NativeImage fsImage;
    private static Identifier fsTextureId;
    private static int fsTexW, fsTexH;
    private static double lastFsPanX = Double.NaN, lastFsPanZ = Double.NaN;
    private static float lastFsZoom = Float.NaN;
    private static boolean fsNeedsUpdate = true;

    public MiniMapFullScreen() {
        super(Component.literal("Map"));
    }

    @Override
    protected void init() {
        // Initialize pan to player position
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && panX == 0 && panZ == 0) {
            panX = mc.player.getX();
            panZ = mc.player.getZ();
        }

        int btnY = this.height - 24;
        int btnH = 20;

        // Create Waypoint button
        this.addRenderableWidget(Button.builder(
            Component.literal("Create Waypoint"),
            btn -> this.minecraft.setScreen(new MiniMapWaypointScreen(this))
        ).bounds(4, btnY, 110, btnH).build());

        // Legend toggle
        this.addRenderableWidget(Button.builder(
            Component.literal(showLegend ? "Legend: On" : "Legend: Off"),
            btn -> {
                showLegend = !showLegend;
                btn.setMessage(Component.literal(showLegend ? "Legend: On" : "Legend: Off"));
            }
        ).bounds(118, btnY, 80, btnH).build());

        // Close button
        this.addRenderableWidget(Button.builder(
            Component.literal("Close"),
            btn -> this.onClose()
        ).bounds(this.width - 54, btnY, 50, btnH).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // Handle mouse drag panning (poll left button state directly)
        long window = GLFW.glfwGetCurrentContext();
        boolean leftDown = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        if (leftDown && dragging) {
            double dx = mouseX - lastMouseX;
            double dy = mouseY - lastMouseY;
            if (dx != 0 || dy != 0) {
                panX -= dx / zoom;
                panZ -= dy / zoom;
            }
        }
        dragging = leftDown;
        lastMouseX = mouseX;
        lastMouseY = mouseY;

        // Texture-based terrain rendering
        int w = this.width;
        int h = this.height;
        ensureFullscreenTexture(w, h);

        boolean viewChanged = Double.isNaN(lastFsPanX)
            || panX != lastFsPanX || panZ != lastFsPanZ || zoom != lastFsZoom;
        if (viewChanged || fsNeedsUpdate) {
            regenerateFullscreenImage(w, h);
            fsTexture.upload();
            lastFsPanX = panX;
            lastFsPanZ = panZ;
            lastFsZoom = zoom;
            fsNeedsUpdate = false;
        }

        graphics.blit(RenderPipelines.GUI_TEXTURED, fsTextureId, 0, 0, 0.0f, 0.0f, w, h, w, h);

        // Draw waypoints
        for (MiniMapWaypoint wp : MiniMapData.getVisibleWaypoints()) {
            int sx = worldToScreenX(wp.x());
            int sy = worldToScreenZ(wp.z());

            if (sx < -10 || sx > this.width + 10 || sy < -10 || sy > this.height + 10) continue;

            int c = wp.color() | 0xFF000000;

            // Diamond marker (7x7)
            graphics.fill(sx - 3, sy, sx + 4, sy + 1, c);
            graphics.fill(sx - 2, sy - 1, sx + 3, sy + 2, c);
            graphics.fill(sx - 1, sy - 2, sx + 2, sy + 3, c);
            graphics.fill(sx, sy - 3, sx + 1, sy + 4, c);

            // Name label
            graphics.drawString(this.font, wp.name(), sx + 6, sy - 4, 0xFFFFFF, true);
        }

        // Draw entities based on mode
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.player != null) {
            ClientLevel level = mc.level;
            int entityMode = MiniMapData.getEntityMode();

            if (entityMode >= 2) {
                // Draw living entities (orange small dot)
                for (Entity entity : level.entitiesForRendering()) {
                    if (entity instanceof Player) continue;
                    if (!(entity instanceof LivingEntity)) continue;
                    int sx = worldToScreenX((int) entity.getX());
                    int sy = worldToScreenZ((int) entity.getZ());
                    if (sx < 0 || sx > this.width || sy < 0 || sy > this.height) continue;
                    graphics.fill(sx - 1, sy - 1, sx + 2, sy + 2, 0xFFFF8800);
                }
            }

            if (entityMode >= 1) {
                // Get all players - use server player list in singleplayer for unlimited range
                List<PlayerPos> playerPositions = getAllPlayerPositions(mc);

                for (PlayerPos pp : playerPositions) {
                    int sx = worldToScreenX((int) pp.x);
                    int sy = worldToScreenZ((int) pp.z);
                    if (sx < 0 || sx > this.width || sy < 0 || sy > this.height) continue;

                    // Cyan filled diamond
                    graphics.fill(sx - 2, sy, sx + 3, sy + 1, 0xFF00FFFF);
                    graphics.fill(sx - 1, sy - 1, sx + 2, sy + 2, 0xFF00FFFF);
                    graphics.fill(sx, sy - 2, sx + 1, sy + 3, 0xFF00FFFF);
                    // White border
                    graphics.fill(sx - 3, sy, sx - 2, sy + 1, 0xFFFFFFFF);
                    graphics.fill(sx + 3, sy, sx + 4, sy + 1, 0xFFFFFFFF);
                    graphics.fill(sx, sy - 3, sx + 1, sy - 2, 0xFFFFFFFF);
                    graphics.fill(sx, sy + 3, sx + 1, sy + 4, 0xFFFFFFFF);
                    // Name tag
                    graphics.drawString(this.font, pp.name, sx + 6, sy - 4, 0xFF00FFFF, true);
                }
            }

            // Draw self marker (green arrow)
            LocalPlayer player = mc.player;
            int selfX = worldToScreenX((int) player.getX());
            int selfZ = worldToScreenZ((int) player.getZ());
            graphics.fill(selfX - 2, selfZ - 2, selfX + 3, selfZ + 3, 0xFF00FF00);

            // Direction indicator
            float yawRad = (float) Math.toRadians(player.getYRot());
            int arrowLen = 6;
            int ax = (int) (-Math.sin(yawRad) * arrowLen);
            int ay = (int) (Math.cos(yawRad) * arrowLen);
            drawLine(graphics, selfX, selfZ, selfX + ax, selfZ + ay, 0xFF00FF00);
        }

        // Coordinates at top-center (from mouse position)
        double mouseWorldX = screenToWorldX(mouseX);
        double mouseWorldZ = screenToWorldZ(mouseY);
        String coords = String.format("X: %d, Z: %d", (int) Math.floor(mouseWorldX), (int) Math.floor(mouseWorldZ));
        graphics.fill(this.width / 2 - 60, 2, this.width / 2 + 60, 16, 0xAA000000);
        graphics.drawCenteredString(this.font, coords, this.width / 2, 5, 0xFFFFFF);

        // Zoom indicator
        String zoomText = zoom >= 0.1f ? String.format("Zoom: %.1fx", zoom)
                                       : String.format("Zoom: %.3fx", zoom);
        int zoomW = this.font.width(zoomText) + 6;
        graphics.fill(2, 2, zoomW + 2, 16, 0xAA000000);
        graphics.drawString(this.font, zoomText, 4, 5, 0xAAAAAA, false);

        // Legend panel
        if (showLegend) {
            int legendX = this.width - 108;
            int legendY = 20;
            MiniMapLegend.render(graphics, this.font, legendX, legendY);
        }

        // Render buttons on top
        super.render(graphics, mouseX, mouseY, delta);
    }

    // --- Input handling ---

    @Override
    public void tick() {
        super.tick();
        // WASD panning via key state polling
        long window = GLFW.glfwGetCurrentContext();
        float panAmount = PAN_SPEED / zoom;
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS) panZ -= panAmount;
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS) panZ += panAmount;
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS) panX -= panAmount;
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS) panX += panAmount;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY > 0) {
            zoom = Math.min(MAX_ZOOM, zoom * 1.3f);
        } else if (scrollY < 0) {
            zoom = Math.max(MIN_ZOOM, zoom / 1.3f);
        }
        return true;
    }


    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // --- Texture helpers ---

    private static void ensureFullscreenTexture(int w, int h) {
        if (fsImage != null && fsTexW == w && fsTexH == h) return;

        if (fsTexture != null) {
            fsTexture.close();
        }
        if (fsTextureId != null) {
            Minecraft.getInstance().getTextureManager().release(fsTextureId);
            fsTextureId = null;
        }

        // Use size constructor so getPixels() returns the internal image
        fsTexture = new DynamicTexture(() -> "superawesomemod_minimap_fs", w, h, false);
        fsImage = fsTexture.getPixels();
        fsTextureId = Identifier.fromNamespaceAndPath("superawesomemod", "minimap_fs");
        Minecraft.getInstance().getTextureManager().register(fsTextureId, fsTexture);
        fsTexW = w;
        fsTexH = h;
        fsNeedsUpdate = true;
    }

    private void regenerateFullscreenImage(int w, int h) {
        double halfW = w / 2.0;
        double halfH = h / 2.0;
        double invZoom = 1.0 / zoom;

        for (int px = 0; px < w; px++) {
            double worldX = panX + (px - halfW) * invZoom;
            int bx = (int) Math.floor(worldX);
            int chunkX = bx >> 4;
            int lx = bx & 15;

            int lastChunkZ = Integer.MIN_VALUE;
            int[] colors = null;

            for (int py = 0; py < h; py++) {
                double worldZ = panZ + (py - halfH) * invZoom;
                int bz = (int) Math.floor(worldZ);
                int cz = bz >> 4;

                if (cz != lastChunkZ) {
                    colors = MiniMapChunkCache.get(chunkX, cz);
                    lastChunkZ = cz;
                }

                int color;
                if (colors != null) {
                    int lz = bz & 15;
                    color = colors[lx * 16 + lz];
                } else {
                    color = 0xFF000000;
                }
                fsImage.setPixelABGR(px, py, argbToAbgr(color));
            }
        }
    }

    private static int argbToAbgr(int argb) {
        int a = (argb >> 24) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        return (a << 24) | (b << 16) | (g << 8) | r;
    }

    // --- Coordinate conversion helpers ---

    private double screenToWorldX(double screenX) {
        return panX + (screenX - this.width / 2.0) / zoom;
    }

    private double screenToWorldZ(double screenY) {
        return panZ + (screenY - this.height / 2.0) / zoom;
    }

    private int worldToScreenX(double worldX) {
        return (int) ((worldX - panX) * zoom + this.width / 2.0);
    }

    private int worldToScreenZ(double worldZ) {
        return (int) ((worldZ - panZ) * zoom + this.height / 2.0);
    }

    // --- Player position helpers (for showing players from any distance) ---

    private record PlayerPos(String name, double x, double z) {}

    private static List<PlayerPos> getAllPlayerPositions(Minecraft mc) {
        List<PlayerPos> positions = new ArrayList<>();

        // In singleplayer, access the integrated server's player list for all player positions
        if (mc.getSingleplayerServer() != null) {
            for (ServerPlayer sp : mc.getSingleplayerServer().getPlayerList().getPlayers()) {
                if (sp.getUUID().equals(mc.player.getUUID())) continue;
                positions.add(new PlayerPos(sp.getName().getString(), sp.getX(), sp.getZ()));
            }
        } else {
            // Multiplayer: use loaded players from client level (limited to server tracking range)
            for (Player p : mc.level.players()) {
                if (p == mc.player) continue;
                positions.add(new PlayerPos(p.getName().getString(), p.getX(), p.getZ()));
            }
        }

        return positions;
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
