package com.steveplays.superawesomemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CombatCrosshairScreen extends Screen {

    private final Screen parent;

    public CombatCrosshairScreen(Screen parent) {
        super(Component.literal("Combat Crosshair"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx   = this.width / 2;
        int cy   = this.height / 2;
        int btnW = 200;
        int btnH = 20;

        int y = cy - 55;

        this.addRenderableWidget(Button.builder(
            toggleLabel(),
            btn -> {
                CombatCrosshairData.setEnabled(!CombatCrosshairData.isEnabled());
                btn.setMessage(toggleLabel());
            }
        ).bounds(cx - btnW / 2, y, btnW, btnH).build());
        y += 25;

        // In-range color cycle
        this.addRenderableWidget(Button.builder(
            inRangeColorLabel(),
            btn -> {
                int next = (CombatCrosshairData.getInRangeColor() + 1) % CombatHitboxData.COLOR_NAMES.length;
                CombatCrosshairData.setInRangeColor(next);
                btn.setMessage(inRangeColorLabel());
            }
        ).bounds(cx - btnW / 2, y, btnW, btnH).build());
        y += 25;

        // Out-of-range color cycle
        this.addRenderableWidget(Button.builder(
            outOfRangeColorLabel(),
            btn -> {
                int next = (CombatCrosshairData.getOutOfRangeColor() + 1) % CombatHitboxData.COLOR_NAMES.length;
                CombatCrosshairData.setOutOfRangeColor(next);
                btn.setMessage(outOfRangeColorLabel());
            }
        ).bounds(cx - btnW / 2, y, btnW, btnH).build());
        y += 25;

        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(cx - 50, y, 100, btnH).build());
    }

    private Component toggleLabel() {
        return Component.literal(CombatCrosshairData.isEnabled()
            ? "Combat Crosshair: Enabled"
            : "Combat Crosshair: Disabled");
    }

    private Component inRangeColorLabel() {
        return Component.literal("In Range: " + CombatCrosshairData.getInRangeColorName());
    }

    private Component outOfRangeColorLabel() {
        return Component.literal("Out of Range: " + CombatCrosshairData.getOutOfRangeColorName());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        int cx = this.width / 2;
        int cy = this.height / 2;
        graphics.drawCenteredString(this.font, this.title, cx, cy - 75, 0xFFFFFF);
        graphics.drawCenteredString(this.font,
            Component.literal("Crosshair color changes when aiming at a target"),
            cx, cy - 61, 0xAAAAAA);
        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
