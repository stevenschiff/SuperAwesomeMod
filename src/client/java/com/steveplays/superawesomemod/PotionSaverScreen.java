package com.steveplays.superawesomemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class PotionSaverScreen extends Screen {

    private final Screen parent;

    public PotionSaverScreen(Screen parent) {
        super(Component.literal("Potion Saver"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx   = this.width  / 2;
        int cy   = this.height / 2;
        int btnW = 200;
        int btnH = 20;

        // Toggle on/off
        this.addRenderableWidget(Button.builder(
            toggleLabel(),
            btn -> {
                PotionSaverData.setEnabled(!PotionSaverData.isEnabled());
                btn.setMessage(toggleLabel());
            }
        ).bounds(cx - btnW / 2, cy - 40, btnW, btnH).build());

        // Back
        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(cx - 50, cy + 20, 100, btnH).build());
    }

    private Component toggleLabel() {
        return Component.literal(PotionSaverData.isEnabled()
            ? "Disable Potion Saver"
            : "Enable Potion Saver");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {

        int cx = this.width / 2;
        int cy = this.height / 2;

        graphics.drawCenteredString(this.font, this.title, cx, cy - 70, 0xFFFFFF);

        boolean on = PotionSaverData.isEnabled();
        // Enabled but powerless is worth saying out loud — on someone else's server
        // the timers aren't ours to hold, and a plain "Enabled" would imply they are.
        boolean inactive = on && this.minecraft.level != null
                && !this.minecraft.hasSingleplayerServer();

        String status = !on ? "Disabled" : inactive ? "Enabled (not active here)" : "Enabled";
        int statusColor = !on ? 0xFF5555 : inactive ? 0xFFAA00 : 0x55FF55;
        graphics.drawCenteredString(this.font, Component.literal("Status: " + status),
            cx, cy - 58, statusColor);

        graphics.drawCenteredString(this.font,
            Component.literal("Good effects stop counting down - bad ones run out"),
            cx, cy - 10, 0xAAAAAA);

        graphics.drawCenteredString(this.font,
            Component.literal("Turtle Master keeps Resistance, drops Slowness"),
            cx, cy + 46, 0xAAAAAA);

        graphics.drawCenteredString(this.font,
            Component.literal("Worlds you host only - servers run their own timers"),
            cx, cy + 58, 0xFFAA00);

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
