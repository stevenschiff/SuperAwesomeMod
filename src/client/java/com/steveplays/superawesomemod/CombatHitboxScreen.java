package com.steveplays.superawesomemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CombatHitboxScreen extends Screen {

    private final Screen parent;

    public CombatHitboxScreen(Screen parent) {
        super(Component.literal("Combat Hitboxes"));
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
                CombatHitboxData.setEnabled(!CombatHitboxData.isEnabled());
                btn.setMessage(toggleLabel());
            }
        ).bounds(cx - btnW / 2, cy - 60, btnW, btnH).build());

        this.addRenderableWidget(Button.builder(
            xrayLabel(),
            btn -> {
                CombatHitboxData.setSeeThroughWalls(!CombatHitboxData.isSeeThroughWalls());
                btn.setMessage(xrayLabel());
            }
        ).bounds(cx - btnW / 2, cy - 35, btnW, btnH).build());

        this.addRenderableWidget(Button.builder(
            scopeLabel(),
            btn -> {
                CombatHitboxData.setPlayersOnly(!CombatHitboxData.isPlayersOnly());
                btn.setMessage(scopeLabel());
            }
        ).bounds(cx - btnW / 2, cy - 10, btnW, btnH).build());

        this.addRenderableWidget(Button.builder(
            invisibleLabel(),
            btn -> {
                CombatHitboxData.setShowInvisible(!CombatHitboxData.isShowInvisible());
                btn.setMessage(invisibleLabel());
            }
        ).bounds(cx - btnW / 2, cy + 15, btnW, btnH).build());

        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(cx - 50, cy + 45, 100, btnH).build());
    }

    private Component toggleLabel() {
        return Component.literal(CombatHitboxData.isEnabled()
            ? "Combat Hitboxes: Enabled"
            : "Combat Hitboxes: Disabled");
    }

    private Component xrayLabel() {
        return Component.literal(CombatHitboxData.isSeeThroughWalls()
            ? "See Through Walls: Enabled"
            : "See Through Walls: Disabled");
    }

    private Component scopeLabel() {
        return Component.literal(CombatHitboxData.isPlayersOnly()
            ? "Show: Players Only"
            : "Show: All Entities");
    }

    private Component invisibleLabel() {
        return Component.literal(CombatHitboxData.isShowInvisible()
            ? "Show Invisible: Enabled"
            : "Show Invisible: Disabled");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);
        int cx = this.width / 2;
        int cy = this.height / 2;
        graphics.drawCenteredString(this.font, this.title, cx, cy - 75, 0xFFFFFF);
        graphics.drawCenteredString(this.font,
            Component.literal("White when out of reach, red when in attack reach"),
            cx, cy - 75 + 14, 0xAAAAAA);
        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
