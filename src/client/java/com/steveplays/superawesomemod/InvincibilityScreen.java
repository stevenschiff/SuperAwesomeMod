package com.steveplays.superawesomemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class InvincibilityScreen extends Screen {

    private final Screen parent;

    public InvincibilityScreen(Screen parent) {
        super(Component.literal("Invincibility"));
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
                InvincibilityData.setEnabled(!InvincibilityData.isEnabled());
                btn.setMessage(toggleLabel());
            }
        ).bounds(cx - btnW / 2, cy - 40, btnW, btnH).build());

        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(cx - 50, cy + 20, 100, btnH).build());
    }

    private Component toggleLabel() {
        return Component.literal(InvincibilityData.isEnabled()
            ? "Disable Invincibility" : "Enable Invincibility");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);

        int cx = this.width / 2;
        int cy = this.height / 2;

        graphics.drawCenteredString(this.font, this.title, cx, cy - 70, 0xFFFFFF);

        boolean on = InvincibilityData.isEnabled();
        boolean host = this.minecraft.hasSingleplayerServer();
        boolean inWorld = this.minecraft.level != null;

        // Deliberately blunt. Believing this is on when it isn't is the worst failure
        // this mod can hand you, so an enabled-but-powerless state never reads green.
        String status;
        int color;
        if (!on) {
            status = "Disabled";
            color = 0xFF5555;
        } else if (inWorld && !host) {
            status = "Enabled - NOT PROTECTING YOU HERE";
            color = 0xFF5555;
        } else {
            status = "Enabled";
            color = 0x55FF55;
        }
        graphics.drawCenteredString(this.font, Component.literal("Status: " + status),
            cx, cy - 58, color);

        graphics.drawCenteredString(this.font,
            Component.literal("Cancels all damage aimed at you"),
            cx, cy + 46, 0xAAAAAA);

        graphics.drawCenteredString(this.font,
            Component.literal("Worlds you host only - a server decides its own damage"),
            cx, cy + 58, 0xFFAA00);

        graphics.drawCenteredString(this.font,
            Component.literal("On a server you will still take damage and still die"),
            cx, cy + 70, 0xFF5555);

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
