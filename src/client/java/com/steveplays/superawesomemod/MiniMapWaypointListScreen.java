package com.steveplays.superawesomemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class MiniMapWaypointListScreen extends Screen {

    private final Screen parent;
    private int scrollOffset = 0;

    private static final int BTN_W = 200;
    private static final int BTN_H = 20;
    private static final int ROW_HEIGHT = 24;
    private static final int VISIBLE_ROWS = 6;

    public MiniMapWaypointListScreen(Screen parent) {
        super(Component.literal("Waypoints"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int topY = this.height / 2 - 80;

        List<MiniMapWaypoint> waypoints = MiniMapData.getWaypoints();

        int end = Math.min(scrollOffset + VISIBLE_ROWS, waypoints.size());
        for (int i = scrollOffset; i < end; i++) {
            MiniMapWaypoint wp = waypoints.get(i);
            int rowY = topY + (i - scrollOffset) * ROW_HEIGHT;
            final int index = i;

            // Waypoint name + coords button (display only)
            String label = wp.name() + " (" + wp.x() + ", " + wp.z() + ")";
            this.addRenderableWidget(Button.builder(
                Component.literal(label),
                btn -> {} // no action, just display
            ).bounds(cx - BTN_W / 2, rowY, BTN_W - 50, BTN_H).build());

            // Delete button
            this.addRenderableWidget(Button.builder(
                Component.literal("Del"),
                btn -> {
                    MiniMapData.removeWaypoint(index);
                    MiniMapPersistence.markDirty();
                    this.rebuildWidgets();
                }
            ).bounds(cx + BTN_W / 2 - 46, rowY, 46, BTN_H).build());
        }

        // Add new waypoint button
        int btnY = topY + VISIBLE_ROWS * ROW_HEIGHT + 8;
        this.addRenderableWidget(Button.builder(
            Component.literal("Add Waypoint"),
            btn -> this.minecraft.setScreen(new MiniMapWaypointScreen(this))
        ).bounds(cx - BTN_W / 2, btnY, BTN_W, BTN_H).build());

        // Back button
        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(cx - 50, btnY + 28, 100, BTN_H).build());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int maxOffset = Math.max(0, MiniMapData.getWaypoints().size() - VISIBLE_ROWS);
        scrollOffset = (int) Math.max(0, Math.min(maxOffset, scrollOffset - scrollY));
        this.rebuildWidgets();
        return true;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);
        int cx = this.width / 2;
        int topY = this.height / 2 - 80;

        graphics.drawCenteredString(this.font, this.title, cx, topY - 16, 0xFFFFFF);

        // Draw color indicators next to each visible waypoint
        List<MiniMapWaypoint> waypoints = MiniMapData.getWaypoints();
        int end = Math.min(scrollOffset + VISIBLE_ROWS, waypoints.size());
        for (int i = scrollOffset; i < end; i++) {
            MiniMapWaypoint wp = waypoints.get(i);
            int rowY = topY + (i - scrollOffset) * ROW_HEIGHT;
            int indicatorX = cx - BTN_W / 2 - 18;
            int color = 0xFF000000 | wp.color();
            graphics.fill(indicatorX, rowY + 2, indicatorX + 14, rowY + 18, color);
        }

        // Scroll indicator
        if (waypoints.size() > VISIBLE_ROWS) {
            String scrollText = (scrollOffset + 1) + "-"
                + Math.min(scrollOffset + VISIBLE_ROWS, waypoints.size())
                + " of " + waypoints.size();
            graphics.drawCenteredString(this.font, scrollText,
                cx, topY + VISIBLE_ROWS * ROW_HEIGHT - 4, 0x888888);
        }

        if (waypoints.isEmpty()) {
            graphics.drawCenteredString(this.font, "No waypoints yet",
                cx, topY + 40, 0x888888);
        }

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
