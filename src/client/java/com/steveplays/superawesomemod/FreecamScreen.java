package com.steveplays.superawesomemod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

public class FreecamScreen extends Screen {

    private final Screen parent;

    private static final String[] SPEED_LABELS = { "Slow", "Normal", "Fast", "Very Fast" };
    private static final float[]  SPEED_VALUES = {
        FreecamData.SLOW, FreecamData.NORMAL, FreecamData.FAST, FreecamData.VERY_FAST
    };

    public FreecamScreen(Screen parent) {
        super(Component.literal("Freecam"));
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
                boolean newState = !FreecamData.isEnabled();
                if (newState) {
                    LocalPlayer p = Minecraft.getInstance().player;
                    if (p != null) {
                        FreecamData.setPos(p.getX(), p.getEyeY(), p.getZ());
                        FreecamData.setRot(p.getYRot(), p.getXRot());
                    }
                }
                FreecamData.setEnabled(newState);
                btn.setMessage(toggleLabel());
            }
        ).bounds(cx - btnW / 2, cy - 40, btnW, btnH).build());

        // Speed preset buttons
        int totalW = SPEED_LABELS.length * 48 + (SPEED_LABELS.length - 1) * 4;
        int startX = cx - totalW / 2;
        for (int i = 0; i < SPEED_LABELS.length; i++) {
            final float speed = SPEED_VALUES[i];
            this.addRenderableWidget(Button.builder(
                Component.literal(SPEED_LABELS[i]),
                btn -> FreecamData.setSpeed(speed)
            ).bounds(startX + i * 52, cy - 10, 48, btnH).build());
        }

        // Back
        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(cx - 50, cy + 20, 100, btnH).build());
    }

    private Component toggleLabel() {
        return Component.literal(FreecamData.isEnabled() ? "Disable Freecam" : "Enable Freecam");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);

        int cx = this.width / 2;
        int cy = this.height / 2;

        graphics.drawCenteredString(this.font, this.title, cx, cy - 70, 0xFFFFFF);

        boolean on = FreecamData.isEnabled();
        graphics.drawCenteredString(this.font,
            Component.literal("Status: " + (on ? "Enabled" : "Disabled")),
            cx, cy - 58, on ? 0x55FF55 : 0xFF5555);

        graphics.drawCenteredString(this.font,
            Component.literal("Speed:"), cx, cy - 24, 0xAAAAAA);
        graphics.drawCenteredString(this.font,
            Component.literal("WASD/Space/Shift to fly  |  Body stays in place"),
            cx, cy + 46, 0xAAAAAA);

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
