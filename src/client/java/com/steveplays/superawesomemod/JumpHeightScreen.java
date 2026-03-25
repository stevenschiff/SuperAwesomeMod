package com.steveplays.superawesomemod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class JumpHeightScreen extends Screen {

    private final Screen parent;
    private EditBox valueField;
    private Component errorMessage = Component.empty();

    public JumpHeightScreen(Screen parent) {
        super(Component.literal("Jump Height"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int cy = this.height / 2;

        // Pre-fill with the player's current multiplier
        Minecraft mc = Minecraft.getInstance();
        float current = (mc.player != null)
            ? PlayerJumpData.getMultiplier(mc.player.getUUID())
            : PlayerJumpData.DEFAULT;

        this.valueField = new EditBox(this.font,
            cx - 50, cy - 10, 100, 20,
            Component.literal("Multiplier"));
        this.valueField.setValue(String.valueOf(current));
        this.valueField.setResponder(value -> this.errorMessage = Component.empty());
        this.addRenderableWidget(this.valueField);

        // Apply
        this.addRenderableWidget(Button.builder(
            Component.literal("Apply"),
            btn -> this.apply()
        ).bounds(cx - 102, cy + 16, 98, 20).build());

        // Reset to 1.0
        this.addRenderableWidget(Button.builder(
            Component.literal("Reset (1.0)"),
            btn -> {
                this.sendJumpCommand(1.0f);
                this.minecraft.setScreen(this.parent);
            }
        ).bounds(cx + 4, cy + 16, 98, 20).build());

        // Back
        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(cx - 50, cy + 42, 100, 20).build());
    }

    private void apply() {
        String raw = this.valueField.getValue().trim();
        try {
            float value = Float.parseFloat(raw);
            if (value < PlayerJumpData.MIN || value > PlayerJumpData.MAX) {
                this.errorMessage = Component.literal(
                    "Value must be " + PlayerJumpData.MIN + " – " + PlayerJumpData.MAX);
                return;
            }
            this.sendJumpCommand(value);
            this.minecraft.setScreen(this.parent);
        } catch (NumberFormatException e) {
            this.errorMessage = Component.literal("Enter a valid number");
        }
    }

    private void sendJumpCommand(float value) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null) {
            mc.getConnection().sendCommand("jumpheight " + value);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);

        int cx = this.width / 2;
        int cy = this.height / 2;

        graphics.drawCenteredString(this.font, this.title, cx, cy - 60, 0xFFFFFF);
        graphics.drawCenteredString(this.font,
            Component.literal("Multiplier  (" + PlayerJumpData.MIN + " – " + PlayerJumpData.MAX + ")"),
            cx, cy - 25, 0xAAAAAA);

        // Error message in red
        if (!this.errorMessage.getString().isEmpty()) {
            graphics.drawCenteredString(this.font, this.errorMessage, cx, cy + 70, 0xFF5555);
        }

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
