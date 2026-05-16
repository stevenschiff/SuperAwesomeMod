package com.steveplays.superawesomemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class MiniMapScreen extends Screen {

    private final Screen parent;

    private static final int BTN_W = 200;
    private static final int BTN_H = 20;

    public MiniMapScreen(Screen parent) {
        super(Component.literal("Mini Map"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int topY = this.height / 2 - 60;
        int gap = 24;

        // Master toggle
        this.addRenderableWidget(Button.builder(
            enabledLabel(),
            btn -> {
                MiniMapData.setEnabled(!MiniMapData.isEnabled());
                btn.setMessage(enabledLabel());
            }
        ).bounds(cx - BTN_W / 2, topY, BTN_W, BTN_H).build());

        // HUD visibility toggle
        this.addRenderableWidget(Button.builder(
            hudLabel(),
            btn -> {
                MiniMapData.setHudVisible(!MiniMapData.isHudVisible());
                btn.setMessage(hudLabel());
            }
        ).bounds(cx - BTN_W / 2, topY + gap, BTN_W, BTN_H).build());

        // Minimap size cycle (64, 96, 128, 160, 192, 224, 256)
        this.addRenderableWidget(Button.builder(
            sizeLabel(),
            btn -> {
                int size = MiniMapData.getMinimapSize() + 32;
                if (size > 256) size = 64;
                MiniMapData.setMinimapSize(size);
                btn.setMessage(sizeLabel());
            }
        ).bounds(cx - BTN_W / 2, topY + gap * 2, BTN_W, BTN_H).build());

        // Corner position cycle
        this.addRenderableWidget(Button.builder(
            cornerLabel(),
            btn -> {
                int c = (MiniMapData.getCorner() + 1) % 4;
                MiniMapData.setCorner(c);
                btn.setMessage(cornerLabel());
            }
        ).bounds(cx - BTN_W / 2, topY + gap * 3, BTN_W, BTN_H).build());

        // Manage Waypoints button
        this.addRenderableWidget(Button.builder(
            Component.literal("Manage Waypoints (" + MiniMapData.getWaypoints().size() + ")"),
            btn -> this.minecraft.setScreen(new MiniMapWaypointListScreen(this))
        ).bounds(cx - BTN_W / 2, topY + gap * 4, BTN_W, BTN_H).build());

        // Back button
        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(cx - 50, topY + gap * 5 + 8, 100, BTN_H).build());
    }

    private Component enabledLabel() {
        return Component.literal(MiniMapData.isEnabled()
            ? "Mini Map: Enabled" : "Mini Map: Disabled");
    }

    private Component hudLabel() {
        return Component.literal(MiniMapData.isHudVisible()
            ? "Corner Map: Shown" : "Corner Map: Hidden");
    }

    private Component sizeLabel() {
        return Component.literal("Minimap Size: " + MiniMapData.getMinimapSize());
    }

    private Component cornerLabel() {
        return Component.literal("Corner: " + MiniMapData.getCornerName());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);
        int cx = this.width / 2;
        int topY = this.height / 2 - 60;

        graphics.drawCenteredString(this.font, this.title, cx, topY - 16, 0xFFFFFF);
        graphics.drawCenteredString(this.font,
            Component.literal("Press C to open full map (when enabled)"),
            cx, topY - 6, 0xAAAAAA);

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
