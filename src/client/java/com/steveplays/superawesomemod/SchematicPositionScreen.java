package com.steveplays.superawesomemod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

/**
 * Screen for editing the schematic placement position, rotation, and mirror.
 */
public class SchematicPositionScreen extends Screen {

    private final Screen parent;
    private EditBox xBox, yBox, zBox;

    private static final int BTN_W = 200;
    private static final int BTN_H = 20;
    private static final int FIELD_W = 60;

    public SchematicPositionScreen(Screen parent) {
        super(Component.literal("Schematic Position"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int topY = this.height / 2 - 70;
        int gap = 24;

        SchematicPlacement placement = SchematicData.getCurrentPlacement();
        BlockPos origin = placement != null ? placement.getOrigin() : BlockPos.ZERO;

        // X / Y / Z edit boxes in a row
        int fieldsY = topY;
        int totalW = FIELD_W * 3 + 8 * 2; // 3 fields + 2 gaps
        int startX = cx - totalW / 2;

        xBox = new EditBox(this.font, startX, fieldsY, FIELD_W, BTN_H, Component.literal("X"));
        xBox.setValue(String.valueOf(origin.getX()));
        this.addRenderableWidget(xBox);

        yBox = new EditBox(this.font, startX + FIELD_W + 8, fieldsY, FIELD_W, BTN_H, Component.literal("Y"));
        yBox.setValue(String.valueOf(origin.getY()));
        this.addRenderableWidget(yBox);

        zBox = new EditBox(this.font, startX + (FIELD_W + 8) * 2, fieldsY, FIELD_W, BTN_H, Component.literal("Z"));
        zBox.setValue(String.valueOf(origin.getZ()));
        this.addRenderableWidget(zBox);

        // Apply position button
        this.addRenderableWidget(Button.builder(
            Component.literal("Apply Position"),
            btn -> applyPosition()
        ).bounds(cx - BTN_W / 2, topY + gap, BTN_W, BTN_H).build());

        // Set to Player Position
        this.addRenderableWidget(Button.builder(
            Component.literal("Set to Player Position"),
            btn -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    BlockPos pos = mc.player.blockPosition();
                    xBox.setValue(String.valueOf(pos.getX()));
                    yBox.setValue(String.valueOf(pos.getY()));
                    zBox.setValue(String.valueOf(pos.getZ()));
                    applyPosition();
                }
            }
        ).bounds(cx - BTN_W / 2, topY + gap * 2, BTN_W, BTN_H).build());

        // Nudge buttons row
        int nudgeY = topY + gap * 3;
        int smallBtn = 30;
        int nudgeStart = cx - (smallBtn * 6 + 4 * 5) / 2;
        this.addRenderableWidget(Button.builder(Component.literal("X-"), btn -> nudge(-1, 0, 0))
            .bounds(nudgeStart, nudgeY, smallBtn, BTN_H).build());
        this.addRenderableWidget(Button.builder(Component.literal("X+"), btn -> nudge(1, 0, 0))
            .bounds(nudgeStart + smallBtn + 4, nudgeY, smallBtn, BTN_H).build());
        this.addRenderableWidget(Button.builder(Component.literal("Y-"), btn -> nudge(0, -1, 0))
            .bounds(nudgeStart + (smallBtn + 4) * 2, nudgeY, smallBtn, BTN_H).build());
        this.addRenderableWidget(Button.builder(Component.literal("Y+"), btn -> nudge(0, 1, 0))
            .bounds(nudgeStart + (smallBtn + 4) * 3, nudgeY, smallBtn, BTN_H).build());
        this.addRenderableWidget(Button.builder(Component.literal("Z-"), btn -> nudge(0, 0, -1))
            .bounds(nudgeStart + (smallBtn + 4) * 4, nudgeY, smallBtn, BTN_H).build());
        this.addRenderableWidget(Button.builder(Component.literal("Z+"), btn -> nudge(0, 0, 1))
            .bounds(nudgeStart + (smallBtn + 4) * 5, nudgeY, smallBtn, BTN_H).build());

        // Rotation cycle
        this.addRenderableWidget(Button.builder(
            rotationLabel(),
            btn -> {
                if (placement != null) {
                    placement.cycleRotation();
                    SchematicVerifier.clearCache();
                    btn.setMessage(rotationLabel());
                }
            }
        ).bounds(cx - BTN_W / 2, topY + gap * 4, BTN_W, BTN_H).build());

        // Mirror toggle
        this.addRenderableWidget(Button.builder(
            mirrorLabel(),
            btn -> {
                if (placement != null) {
                    placement.toggleMirror();
                    SchematicVerifier.clearCache();
                    btn.setMessage(mirrorLabel());
                }
            }
        ).bounds(cx - BTN_W / 2, topY + gap * 5, BTN_W, BTN_H).build());

        // Back
        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(cx - 50, topY + gap * 6 + 8, 100, BTN_H).build());
    }

    private void applyPosition() {
        SchematicPlacement placement = SchematicData.getCurrentPlacement();
        if (placement == null) return;

        try {
            int x = Integer.parseInt(xBox.getValue().trim());
            int y = Integer.parseInt(yBox.getValue().trim());
            int z = Integer.parseInt(zBox.getValue().trim());
            placement.setOrigin(new BlockPos(x, y, z));
            SchematicVerifier.clearCache();
        } catch (NumberFormatException ignored) {
            // Invalid input, do nothing
        }
    }

    private void nudge(int dx, int dy, int dz) {
        SchematicPlacement placement = SchematicData.getCurrentPlacement();
        if (placement == null) return;

        BlockPos origin = placement.getOrigin();
        BlockPos newOrigin = origin.offset(dx, dy, dz);
        placement.setOrigin(newOrigin);
        SchematicVerifier.clearCache();

        xBox.setValue(String.valueOf(newOrigin.getX()));
        yBox.setValue(String.valueOf(newOrigin.getY()));
        zBox.setValue(String.valueOf(newOrigin.getZ()));
    }

    private Component rotationLabel() {
        SchematicPlacement placement = SchematicData.getCurrentPlacement();
        String rot = placement != null ? placement.getRotationName() : "0°";
        return Component.literal("Rotation: " + rot);
    }

    private Component mirrorLabel() {
        SchematicPlacement placement = SchematicData.getCurrentPlacement();
        boolean m = placement != null && placement.isMirror();
        return Component.literal(m ? "Mirror: On" : "Mirror: Off");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);
        int cx = this.width / 2;
        int topY = this.height / 2 - 70;

        graphics.drawCenteredString(this.font, this.title, cx, topY - 16, 0xFFFFFF);

        // Field labels
        int totalW = FIELD_W * 3 + 8 * 2;
        int startX = cx - totalW / 2;
        graphics.drawString(this.font, "X", startX, topY - 10, 0xAAAAAA);
        graphics.drawString(this.font, "Y", startX + FIELD_W + 8, topY - 10, 0xAAAAAA);
        graphics.drawString(this.font, "Z", startX + (FIELD_W + 8) * 2, topY - 10, 0xAAAAAA);

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
