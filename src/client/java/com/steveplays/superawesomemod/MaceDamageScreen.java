package com.steveplays.superawesomemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class MaceDamageScreen extends Screen {

    private final Screen parent;

    public MaceDamageScreen(Screen parent) {
        super(Component.literal("Mace Damage"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx   = this.width  / 2;
        int cy   = this.height / 2;
        int btnW = 200;
        int btnH = 20;

        this.addRenderableWidget(Button.builder(
            toggleLabel(),
            btn -> {
                MaceDamageData.setEnabled(!MaceDamageData.isEnabled());
                btn.setMessage(toggleLabel());
            }
        ).bounds(cx - btnW / 2, cy - 40, btnW, btnH).build());

        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(cx - 50, cy + 20, 100, btnH).build());
    }

    private Component toggleLabel() {
        return Component.literal(MaceDamageData.isEnabled()
            ? "Disable Mace Damage" : "Enable Mace Damage");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);

        int cx = this.width / 2;
        int cy = this.height / 2;

        graphics.drawCenteredString(this.font, this.title, cx, cy - 82, 0xFFFFFF);

        boolean on = MaceDamageData.isEnabled();
        graphics.drawCenteredString(this.font,
            Component.literal("Status: " + (on ? "Enabled" : "Disabled")),
            cx, cy - 70, on ? 0x55FF55 : 0xFF5555);

        // The charge readout is the useful part: it says what your next hit does.
        double held = MaceDamageHandler.getEstimate();
        double bonus = MaceDamageData.bonusDamageAt(held);
        boolean armed = held > MaceDamageData.SMASH_THRESHOLD;

        graphics.drawCenteredString(this.font,
            Component.literal(String.format("Held fall distance: %.1f blocks", held)),
            cx, cy - 10, armed ? 0x55FF55 : 0xAAAAAA);

        graphics.drawCenteredString(this.font,
            Component.literal(armed
                ? String.format("Next smash: +%.0f bonus damage", bonus)
                : "Not armed - fall 2+ blocks once to charge it"),
            cx, cy + 2, armed ? 0x55FF55 : 0xFFAA00);

        graphics.drawCenteredString(this.font,
            Component.literal("Keeps the fall distance instead of losing it on landing"),
            cx, cy + 46, 0xAAAAAA);

        graphics.drawCenteredString(this.font,
            Component.literal("Only while holding a mace  |  works on any server"),
            cx, cy + 58, 0xAAAAAA);

        graphics.drawCenteredString(this.font,
            Component.literal("No multiplier exists - the server sets damage from the fall"),
            cx, cy + 70, 0xFFAA00);

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
