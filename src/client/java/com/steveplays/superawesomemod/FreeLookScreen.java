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
            modeLabel(),
            btn -> {
                FreeLookData.setToggleMode(!FreeLookData.isToggleMode());
                FreeLookData.setActive(false);
                FreeLookData.reset();
                btn.setMessage(modeLabel());
            }
        ).bounds(cx - btnW / 2, cy - 20, btnW, btnH).build());

        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(cx - 50, cy + 10, 100, btnH).build());
    }

    private Component modeLabel() {
        return Component.literal("Mode: " + (FreeLookData.isToggleMode() ? "Toggle" : "Hold"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);
        int cx = this.width / 2;
        int cy = this.height / 2;
        graphics.drawCenteredString(this.font, this.title, cx, cy - 50, 0xFFFFFF);
        graphics.drawCenteredString(this.font,
            Component.literal("Key: V  (rebindable in Options \u2192 Controls)"),
            cx, cy - 36, 0xAAAAAA);
        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
