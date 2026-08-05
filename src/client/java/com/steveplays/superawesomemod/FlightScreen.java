package com.steveplays.superawesomemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class FlightScreen extends Screen {

    private final Screen parent;
    private EditBox speedField;

    public FlightScreen(Screen parent) {
        super(Component.literal("Flight"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx   = this.width  / 2;
        int cy   = this.height / 2;
        int btnW = 200;
        int btnH = 20;

        // Toggle on/off — picks up whatever is typed in the speed box
        this.addRenderableWidget(Button.builder(
            toggleLabel(),
            btn -> {
                applySpeed();
                FlightData.setEnabled(!FlightData.isEnabled());
                btn.setMessage(toggleLabel());
            }
        ).bounds(cx - btnW / 2, cy - 44, btnW, btnH).build());

        // Speed input box (1-10000 blocks/second)
        speedField = new EditBox(this.font, cx - 40, cy - 6, 80, btnH,
                Component.literal("Speed"));
        speedField.setMaxLength(5);
        speedField.setValue(String.valueOf(FlightData.getBlocksPerSecond()));
        speedField.setResponder(text -> applySpeed());
        this.addRenderableWidget(speedField);

        // Back
        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> {
                applySpeed();
                this.minecraft.setScreen(this.parent);
            }
        ).bounds(cx - 50, cy + 22, 100, btnH).build());
    }

    /**
     * Reads the speed box and stores it. Empty or non-numeric text is ignored so
     * half-typed values don't wipe the current speed.
     */
    private void applySpeed() {
        if (speedField == null) return;
        String text = speedField.getValue().trim();
        if (text.isEmpty()) return;
        try {
            FlightData.setBlocksPerSecond(Integer.parseInt(text));
        } catch (NumberFormatException ignored) {
            // keep the previous speed
        }
    }

    @Override
    public void onClose() {
        applySpeed();
        super.onClose();
    }

    private Component toggleLabel() {
        return Component.literal(FlightData.isEnabled() ? "Disable Flight" : "Enable Flight");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);

        int cx = this.width / 2;
        int cy = this.height / 2;

        graphics.drawCenteredString(this.font, this.title, cx, cy - 70, 0xFFFFFF);

        boolean on = FlightData.isEnabled();
        graphics.drawCenteredString(this.font,
            Component.literal("Status: " + (on ? "Enabled" : "Disabled")),
            cx, cy - 58, on ? 0x55FF55 : 0xFF5555);

        graphics.drawCenteredString(this.font,
            Component.literal("Speed (blocks/sec)"),
            cx, cy - 18, 0xFFFFFF);

        int speed = FlightData.getBlocksPerSecond();
        graphics.drawCenteredString(this.font,
            Component.literal("Using " + speed + " blocks/sec  |  "
                + FlightData.MIN_BLOCKS_PER_SECOND + "-" + FlightData.MAX_BLOCKS_PER_SECOND),
            cx, cy + 48, speed > 1000 ? 0xFFAA00 : 0xAAAAAA);

        graphics.drawCenteredString(this.font,
            Component.literal("WASD/Space/Shift to fly  |  Works in survival"),
            cx, cy + 60, 0xAAAAAA);

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
