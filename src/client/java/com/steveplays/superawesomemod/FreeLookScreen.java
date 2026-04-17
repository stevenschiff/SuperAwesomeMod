package com.steveplays.superawesomemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class FreeLookScreen extends Screen {

    private final Screen parent;

    public FreeLookScreen(Screen parent) {
        super(Component.literal("Free Look"));
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
                FreeLookData.setEnabled(!FreeLookData.isEnabled());
                FreeLookData.reset();
                btn.setMessage(toggleLabel());
            }
        ).bounds(cx - btnW / 2, cy - 20, btnW, btnH).build());

        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(cx - 50, cy + 10, 100, btnH).build());
    }

    private Component toggleLabel() {
        return Component.literal(FreeLookData.isEnabled() ? "Free Look: Enabled" : "Free Look: Disabled");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);
        int cx = this.width / 2;
        int cy = this.height / 2;
        graphics.drawCenteredString(this.font, this.title, cx, cy - 50, 0xFFFFFF);
        graphics.drawCenteredString(this.font,
            Component.literal("Only active in third-person view"),
            cx, cy - 36, 0xAAAAAA);
        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
