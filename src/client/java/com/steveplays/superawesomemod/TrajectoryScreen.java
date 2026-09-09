package com.steveplays.superawesomemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TrajectoryScreen extends Screen {

    private final Screen parent;

    public TrajectoryScreen(Screen parent) {
        super(Component.literal("Trajectory Preview"));
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
                TrajectoryData.setEnabled(!TrajectoryData.isEnabled());
                btn.setMessage(toggleLabel());
            }
        ).bounds(cx - btnW / 2, cy - 40, btnW, btnH).build());

        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(cx - 50, cy + 20, 100, btnH).build());
    }

    private Component toggleLabel() {
        return Component.literal(TrajectoryData.isEnabled()
            ? "Disable Trajectory Preview" : "Enable Trajectory Preview");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {

        int cx = this.width / 2;
        int cy = this.height / 2;

        graphics.drawCenteredString(this.font, this.title, cx, cy - 70, 0xFFFFFF);

        boolean on = TrajectoryData.isEnabled();
        graphics.drawCenteredString(this.font,
            Component.literal("Status: " + (on ? "Enabled" : "Disabled")),
            cx, cy - 58, on ? 0x55FF55 : 0xFF5555);

        graphics.drawCenteredString(this.font,
            Component.literal("Bow, crossbow, trident, pearl, snowball, egg, potions"),
            cx, cy - 10, 0xAAAAAA);

        graphics.drawCenteredString(this.font,
            Component.literal("Bow arc follows your draw  |  red cross marks impact"),
            cx, cy + 46, 0xAAAAAA);

        graphics.drawCenteredString(this.font,
            Component.literal("Nothing is sent to the server  |  works anywhere"),
            cx, cy + 58, 0xAAAAAA);

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
