package com.steveplays.superawesomemod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class MiniMapWaypointScreen extends Screen {

    private final Screen parent;
    private EditBox nameField;
    private EditBox xField;
    private EditBox zField;

    private int colorR = 255;
    private int colorG = 0;
    private int colorB = 0;

    private static final int BTN_W = 200;
    private static final int BTN_H = 20;

    public MiniMapWaypointScreen(Screen parent) {
        super(Component.literal("Create Waypoint"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int topY = this.height / 2 - 80;

        // Name field
        nameField = new EditBox(this.font, cx - BTN_W / 2, topY, BTN_W, BTN_H,
            Component.literal("Name"));
        nameField.setHint(Component.literal("Waypoint name..."));
        nameField.setMaxLength(32);
        this.addRenderableWidget(nameField);

        // X coordinate field
        xField = new EditBox(this.font, cx - BTN_W / 2, topY + 28, 95, BTN_H,
            Component.literal("X"));
        xField.setHint(Component.literal("X coordinate"));
        // Pre-fill with player position
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            xField.setValue(String.valueOf((int) mc.player.getX()));
            zField = new EditBox(this.font, cx - BTN_W / 2 + 105, topY + 28, 95, BTN_H,
                Component.literal("Z"));
            zField.setHint(Component.literal("Z coordinate"));
            zField.setValue(String.valueOf((int) mc.player.getZ()));
        } else {
            zField = new EditBox(this.font, cx - BTN_W / 2 + 105, topY + 28, 95, BTN_H,
                Component.literal("Z"));
            zField.setHint(Component.literal("Z coordinate"));
            zField.setValue("0");
            xField.setValue("0");
        }
        this.addRenderableWidget(xField);
        this.addRenderableWidget(zField);

        // Color buttons (R/G/B increment/decrement)
        int colorY = topY + 60;

        // Red
        this.addRenderableWidget(Button.builder(Component.literal("R-"),
            btn -> { colorR = Math.max(0, colorR - 32); })
            .bounds(cx - BTN_W / 2, colorY, 40, BTN_H).build());
        this.addRenderableWidget(Button.builder(Component.literal("R+"),
            btn -> { colorR = Math.min(255, colorR + 32); })
            .bounds(cx - BTN_W / 2 + 44, colorY, 40, BTN_H).build());

        // Green
        this.addRenderableWidget(Button.builder(Component.literal("G-"),
            btn -> { colorG = Math.max(0, colorG - 32); })
            .bounds(cx - BTN_W / 2 + 90, colorY, 40, BTN_H).build());
        this.addRenderableWidget(Button.builder(Component.literal("G+"),
            btn -> { colorG = Math.min(255, colorG + 32); })
            .bounds(cx - BTN_W / 2 + 134, colorY, 40, BTN_H).build());

        // Blue
        this.addRenderableWidget(Button.builder(Component.literal("B-"),
            btn -> { colorB = Math.max(0, colorB - 32); })
            .bounds(cx - BTN_W / 2 + 180, colorY, 40, BTN_H).build());
        this.addRenderableWidget(Button.builder(Component.literal("B+"),
            btn -> { colorB = Math.min(255, colorB + 32); })
            .bounds(cx - BTN_W / 2 + 224, colorY, 40, BTN_H).build());

        // Save button
        int btnY = topY + 110;
        this.addRenderableWidget(Button.builder(Component.literal("Save"),
            btn -> {
                saveWaypoint();
                this.minecraft.setScreen(this.parent);
            }).bounds(cx - BTN_W / 2, btnY, 95, BTN_H).build());

        // Cancel button
        this.addRenderableWidget(Button.builder(Component.literal("Cancel"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(cx - BTN_W / 2 + 105, btnY, 95, BTN_H).build());
    }

    private void saveWaypoint() {
        String name = nameField.getValue().trim();
        if (name.isEmpty()) name = "Waypoint";

        int x, z;
        try {
            x = Integer.parseInt(xField.getValue().trim());
        } catch (NumberFormatException e) {
            x = 0;
        }
        try {
            z = Integer.parseInt(zField.getValue().trim());
        } catch (NumberFormatException e) {
            z = 0;
        }

        int color = (colorR << 16) | (colorG << 8) | colorB;
        MiniMapData.addWaypoint(new MiniMapWaypoint(name, x, z, color));
        MiniMapPersistence.markDirty();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);

        int cx = this.width / 2;
        int topY = this.height / 2 - 80;

        // Title
        graphics.drawCenteredString(this.font, this.title, cx, topY - 16, 0xFFFFFF);

        // Labels
        graphics.drawString(this.font, "Name:", cx - BTN_W / 2 - 35, topY + 6, 0xAAAAAA, false);
        graphics.drawString(this.font, "Pos:", cx - BTN_W / 2 - 30, topY + 34, 0xAAAAAA, false);

        // Color preview
        int colorY = topY + 60;
        int previewColor = 0xFF000000 | (colorR << 16) | (colorG << 8) | colorB;
        graphics.drawString(this.font, "Color:", cx - BTN_W / 2 - 35, colorY + 6, 0xAAAAAA, false);

        // Color swatch below the buttons
        int swatchY = colorY + 24;
        graphics.fill(cx - 20, swatchY, cx + 20, swatchY + 16, previewColor);
        // Border
        graphics.fill(cx - 20, swatchY, cx + 20, swatchY + 1, 0xFF888888);
        graphics.fill(cx - 20, swatchY + 15, cx + 20, swatchY + 16, 0xFF888888);
        graphics.fill(cx - 20, swatchY, cx - 19, swatchY + 16, 0xFF888888);
        graphics.fill(cx + 19, swatchY, cx + 20, swatchY + 16, 0xFF888888);

        // RGB values text
        String rgbText = String.format("R:%d G:%d B:%d", colorR, colorG, colorB);
        graphics.drawCenteredString(this.font, rgbText, cx, swatchY + 20, 0xCCCCCC);

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
