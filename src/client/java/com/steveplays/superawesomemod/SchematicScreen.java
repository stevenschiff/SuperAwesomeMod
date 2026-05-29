package com.steveplays.superawesomemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Main settings screen for the Schematics feature.
 * Follows the same pattern as {@link MiniMapScreen}.
 */
public class SchematicScreen extends Screen {

    private final Screen parent;

    private static final int BTN_W = 200;
    private static final int BTN_H = 20;

    public SchematicScreen(Screen parent) {
        super(Component.literal("Schematics"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int topY = this.height / 2 - 84;
        int gap = 24;

        // Master toggle
        this.addRenderableWidget(Button.builder(
            enabledLabel(),
            btn -> {
                SchematicData.setEnabled(!SchematicData.isEnabled());
                btn.setMessage(enabledLabel());
            }
        ).bounds(cx - BTN_W / 2, topY, BTN_W, BTN_H).build());

        // Browse Schematics
        this.addRenderableWidget(Button.builder(
            Component.literal("Browse Schematics"),
            btn -> this.minecraft.setScreen(new SchematicBrowserScreen(this))
        ).bounds(cx - BTN_W / 2, topY + gap, BTN_W, BTN_H).build());

        // Position / Rotate
        Button posBtn = Button.builder(
            Component.literal("Position / Rotate"),
            btn -> this.minecraft.setScreen(new SchematicPositionScreen(this))
        ).bounds(cx - BTN_W / 2, topY + gap * 2, BTN_W, BTN_H).build();
        posBtn.active = SchematicData.getCurrentPlacement() != null;
        this.addRenderableWidget(posBtn);

        // Material List
        Button matBtn = Button.builder(
            Component.literal("Material List"),
            btn -> this.minecraft.setScreen(new SchematicMaterialListScreen(this))
        ).bounds(cx - BTN_W / 2, topY + gap * 3, BTN_W, BTN_H).build();
        matBtn.active = SchematicData.getCurrentPlacement() != null;
        this.addRenderableWidget(matBtn);

        // Render Mode cycle
        this.addRenderableWidget(Button.builder(
            renderModeLabel(),
            btn -> {
                SchematicData.cycleRenderMode();
                btn.setMessage(renderModeLabel());
            }
        ).bounds(cx - BTN_W / 2, topY + gap * 4, BTN_W, BTN_H).build());

        // Layer Mode toggle
        this.addRenderableWidget(Button.builder(
            layerModeLabel(),
            btn -> {
                SchematicData.setLayerMode(!SchematicData.isLayerMode());
                btn.setMessage(layerModeLabel());
            }
        ).bounds(cx - BTN_W / 2, topY + gap * 5, BTN_W, BTN_H).build());

        // Ghost Alpha cycle
        this.addRenderableWidget(Button.builder(
            alphaLabel(),
            btn -> {
                SchematicData.cycleGhostAlpha();
                btn.setMessage(alphaLabel());
            }
        ).bounds(cx - BTN_W / 2, topY + gap * 6, BTN_W, BTN_H).build());

        // Easy Place toggle
        this.addRenderableWidget(Button.builder(
            easyPlaceLabel(),
            btn -> {
                SchematicData.setEasyPlaceEnabled(!SchematicData.isEasyPlaceEnabled());
                btn.setMessage(easyPlaceLabel());
                this.rebuildWidgets();
            }
        ).bounds(cx - BTN_W / 2, topY + gap * 7, BTN_W, BTN_H).build());

        // Allow on Server toggle (greyed out when Easy Place is off)
        Button serverBtn = Button.builder(
            allowOnServerLabel(),
            btn -> {
                SchematicData.setEasyPlaceAllowOnServer(!SchematicData.isEasyPlaceAllowOnServer());
                btn.setMessage(allowOnServerLabel());
            }
        ).bounds(cx - BTN_W / 2, topY + gap * 8, BTN_W, BTN_H).build();
        serverBtn.active = SchematicData.isEasyPlaceEnabled();
        this.addRenderableWidget(serverBtn);

        // Back
        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(cx - 50, topY + gap * 9 + 8, 100, BTN_H).build());
    }

    private Component enabledLabel() {
        return Component.literal(SchematicData.isEnabled()
            ? "Schematics: Enabled" : "Schematics: Disabled");
    }

    private Component renderModeLabel() {
        return Component.literal("Render Mode: " + SchematicData.getRenderModeName());
    }

    private Component layerModeLabel() {
        return Component.literal(SchematicData.isLayerMode()
            ? "Layer Mode: On" : "Layer Mode: Off");
    }

    private Component alphaLabel() {
        return Component.literal("Ghost Alpha: " + String.format("%.1f", SchematicData.getGhostAlpha()));
    }

    private Component easyPlaceLabel() {
        return Component.literal(SchematicData.isEasyPlaceEnabled()
            ? "Easy Place: On" : "Easy Place: Off");
    }

    private Component allowOnServerLabel() {
        return Component.literal(SchematicData.isEasyPlaceAllowOnServer()
            ? "Allow on Server: On" : "Allow on Server: Off");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);
        int cx = this.width / 2;
        int topY = this.height / 2 - 84;

        graphics.drawCenteredString(this.font, this.title, cx, topY - 16, 0xFFFFFF);

        SchematicPlacement p = SchematicData.getCurrentPlacement();
        if (p != null) {
            graphics.drawCenteredString(this.font,
                Component.literal("Loaded: " + p.getSchematic().getName()),
                cx, topY - 6, 0xAAAAAA);
        }

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
