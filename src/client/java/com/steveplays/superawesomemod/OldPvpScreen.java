package com.steveplays.superawesomemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class OldPvpScreen extends Screen {

    private final Screen parent;

    public OldPvpScreen(Screen parent) {
        super(Component.literal("1.7 PvP Animations"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx   = this.width / 2;
        int cy   = this.height / 2;
        int btnW = 200;
        int btnH = 20;
        int gap  = 24;

        int row0 = cy - 30;

        this.addRenderableWidget(Button.builder(
            blockingLabel(),
            btn -> {
                OldPvpData.setBlockingEnabled(!OldPvpData.isBlockingEnabled());
                btn.setMessage(blockingLabel());
            }
        ).bounds(cx - btnW / 2, row0, btnW, btnH).build());

        this.addRenderableWidget(Button.builder(
            swingLabel(),
            btn -> {
                OldPvpData.setSwingWhileUsingEnabled(!OldPvpData.isSwingWhileUsingEnabled());
                btn.setMessage(swingLabel());
            }
        ).bounds(cx - btnW / 2, row0 + gap, btnW, btnH).build());

        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(cx - 50, row0 + gap * 2 + 10, 100, btnH).build());
    }

    private Component blockingLabel() {
        return Component.literal(OldPvpData.isBlockingEnabled()
            ? "Sword Blocking: Enabled" : "Sword Blocking: Disabled");
    }

    private Component swingLabel() {
        return Component.literal(OldPvpData.isSwingWhileUsingEnabled()
            ? "Swing While Using: Enabled" : "Swing While Using: Disabled");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);
        int cx = this.width / 2;
        int cy = this.height / 2;
        graphics.drawCenteredString(this.font, this.title, cx, cy - 66, 0xFFFFFF);
        graphics.drawCenteredString(this.font,
            Component.literal("Classic 1.7 sword blocking & swing animations"),
            cx, cy - 52, 0xAAAAAA);
        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
