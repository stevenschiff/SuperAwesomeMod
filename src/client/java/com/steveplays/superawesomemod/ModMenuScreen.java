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
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int buttonW = 200;
        int buttonH = 20;
        int spacing = 24;

        // --- Feature buttons (add more here as the mod grows) ---
        this.addRenderableWidget(Button.builder(
            Component.literal("Jump Height"),
            btn -> this.minecraft.setScreen(new JumpHeightScreen(this))
        ).bounds(centerX - buttonW / 2, centerY - spacing, buttonW, buttonH).build());

        // --- Close ---
        this.addRenderableWidget(Button.builder(
            Component.literal("Close"),
            btn -> this.onClose()
        ).bounds(centerX - buttonW / 2, centerY + spacing - buttonH, buttonW, buttonH).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);
        graphics.drawCenteredString(this.font, this.title,
            this.width / 2, this.height / 2 - 60, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;  // keep the game running while the menu is open
    }
}
