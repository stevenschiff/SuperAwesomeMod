package com.steveplays.superawesomemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class PvpDetectorScreen extends Screen {

    private final Screen parent;

    public PvpDetectorScreen(Screen parent) {
        super(Component.literal("PvP Cheat Detector"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx   = this.width / 2;
        int cy   = this.height / 2;
        int btnW = 200;
        int btnH = 20;

        this.addRenderableWidget(Button.builder(
            toggleLabel(),
            btn -> {
                PvpDetectorData.setEnabled(!PvpDetectorData.isEnabled());
                btn.setMessage(toggleLabel());
            }
        ).bounds(cx - btnW / 2, cy - 30, btnW, btnH).build());

        this.addRenderableWidget(Button.builder(
            Component.literal("Reset Stats"),
            btn -> PvpDetectorTracker.clear()
        ).bounds(cx - btnW / 2, cy, btnW, btnH).build());

        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(cx - 50, cy + 30, 100, btnH).build());
    }

    private Component toggleLabel() {
        return Component.literal(PvpDetectorData.isEnabled()
            ? "PvP Detector: Enabled"
            : "PvP Detector: Disabled");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);
        int cx = this.width / 2;
        int cy = this.height / 2;
        graphics.drawCenteredString(this.font, this.title, cx, cy - 70, 0xFFFFFF);
        graphics.drawCenteredString(this.font,
            Component.literal("Watches other players' attack reach, ping, and line-of-sight."),
            cx, cy - 56, 0xAAAAAA);
        graphics.drawCenteredString(this.font,
            Component.literal("Heuristic — long reach + many hits raises the suspicion band."),
            cx, cy - 44, 0xAAAAAA);
        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
