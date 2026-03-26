package com.steveplays.superawesomemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Main mod menu screen. Add a new Button entry here for each future feature.
 * Open with the G keybind (rebindable in Options → Controls → SuperAwesomeMod).
 */
public class ModMenuScreen extends Screen {

    public ModMenuScreen() {
        super(Component.literal("SuperAwesomeMod"));
    }

    @Override
    protected void init() {
        int cx    = this.width / 2;
        int cy    = this.height / 2;
        int btnW  = 200;
        int btnH  = 20;
        int gap   = 26;

        // --- Feature buttons (add more below as the mod grows) ---
        this.addRenderableWidget(Button.builder(
            Component.literal("Jump Height"),
            btn -> this.minecraft.setScreen(new JumpHeightScreen(this))
        ).bounds(cx - btnW / 2, cy - gap * 2, btnW, btnH).build());

        this.addRenderableWidget(Button.builder(
            Component.literal("Flight"),
            btn -> this.minecraft.setScreen(new FlyScreen(this))
        ).bounds(cx - btnW / 2, cy - gap, btnW, btnH).build());

        this.addRenderableWidget(Button.builder(
            Component.literal("Attack Range"),
            btn -> this.minecraft.setScreen(new AttackRangeScreen(this))
        ).bounds(cx - btnW / 2, cy, btnW, btnH).build());

        // --- Close ---
        this.addRenderableWidget(Button.builder(
            Component.literal("Close"),
            btn -> this.onClose()
        ).bounds(cx - btnW / 2, cy + gap * 2, btnW, btnH).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);
        graphics.drawCenteredString(this.font, this.title,
            this.width / 2, this.height / 2 - 80, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
