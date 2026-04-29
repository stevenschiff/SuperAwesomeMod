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
        int gap   = 24;

        // 6 buttons centered vertically around cy: rows -2.5..+2.5 of `gap`.
        int row0 = cy - (5 * gap) / 2;

        // --- Feature buttons (add more below as the mod grows) ---
        this.addRenderableWidget(Button.builder(
            Component.literal("Jump Height"),
            btn -> this.minecraft.setScreen(new JumpHeightScreen(this))
        ).bounds(cx - btnW / 2, row0, btnW, btnH).build());

        this.addRenderableWidget(Button.builder(
            Component.literal("Flight"),
            btn -> this.minecraft.setScreen(new FlyScreen(this))
        ).bounds(cx - btnW / 2, row0 + gap, btnW, btnH).build());

        this.addRenderableWidget(Button.builder(
            Component.literal("Attack Range"),
            btn -> this.minecraft.setScreen(new AttackRangeScreen(this))
        ).bounds(cx - btnW / 2, row0 + gap * 2, btnW, btnH).build());

        this.addRenderableWidget(Button.builder(
            Component.literal("Free Look"),
            btn -> this.minecraft.setScreen(new FreeLookScreen(this))
        ).bounds(cx - btnW / 2, row0 + gap * 3, btnW, btnH).build());

        this.addRenderableWidget(Button.builder(
            Component.literal("Armor HUD"),
            btn -> this.minecraft.setScreen(new ArmorHudScreen(this))
        ).bounds(cx - btnW / 2, row0 + gap * 4, btnW, btnH).build());

        // --- Close ---
        this.addRenderableWidget(Button.builder(
            Component.literal("Close"),
            btn -> this.onClose()
        ).bounds(cx - btnW / 2, row0 + gap * 5, btnW, btnH).build());
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
