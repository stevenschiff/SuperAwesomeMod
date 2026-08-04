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

        int y = cy - 80;

        this.addRenderableWidget(Button.builder(
            toggleLabel(),
            btn -> {
                CombatHitboxData.setEnabled(!CombatHitboxData.isEnabled());
                btn.setMessage(toggleLabel());
            }
        ).bounds(cx - btnW / 2, y, btnW, btnH).build());
        y += 25;

        this.addRenderableWidget(Button.builder(
            xrayLabel(),
            btn -> {
                CombatHitboxData.setSeeThroughWalls(!CombatHitboxData.isSeeThroughWalls());
                btn.setMessage(xrayLabel());
            }
        ).bounds(cx - btnW / 2, y, btnW, btnH).build());
        y += 25;

        this.addRenderableWidget(Button.builder(
            scopeLabel(),
            btn -> {
                CombatHitboxData.setPlayersOnly(!CombatHitboxData.isPlayersOnly());
                btn.setMessage(scopeLabel());
            }
        ).bounds(cx - btnW / 2, y, btnW, btnH).build());
        y += 25;

        this.addRenderableWidget(Button.builder(
            invisibleLabel(),
            btn -> {
                CombatHitboxData.setShowInvisible(!CombatHitboxData.isShowInvisible());
                btn.setMessage(invisibleLabel());
            }
        ).bounds(cx - btnW / 2, y, btnW, btnH).build());
        y += 25;

        // In-range color cycle
        this.addRenderableWidget(Button.builder(
            inRangeColorLabel(),
            btn -> {
                int next = (CombatHitboxData.getInRangeColor() + 1) % CombatHitboxData.COLOR_NAMES.length;
                CombatHitboxData.setInRangeColor(next);
                btn.setMessage(inRangeColorLabel());
            }
        ).bounds(cx - btnW / 2, y, btnW, btnH).build());
        y += 25;

        // Out-of-range color cycle
        this.addRenderableWidget(Button.builder(
            outOfRangeColorLabel(),
            btn -> {
                int next = (CombatHitboxData.getOutOfRangeColor() + 1) % CombatHitboxData.COLOR_NAMES.length;
                CombatHitboxData.setOutOfRangeColor(next);
                btn.setMessage(outOfRangeColorLabel());
            }
        ).bounds(cx - btnW / 2, y, btnW, btnH).build());
        y += 25;

        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(cx - 50, y, 100, btnH).build());
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

    private Component inRangeColorLabel() {
        return Component.literal("In Range: " + CombatHitboxData.getInRangeColorName());
    }

    private Component outOfRangeColorLabel() {
        return Component.literal("Out of Range: " + CombatHitboxData.getOutOfRangeColorName());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);
        int cx = this.width / 2;
        int cy = this.height / 2;
        graphics.drawCenteredString(this.font, this.title, cx, cy - 100, 0xFFFFFF);
        graphics.drawCenteredString(this.font,
            Component.literal("Customizable hitbox colors for in/out of attack range"),
            cx, cy - 100 + 14, 0xAAAAAA);
        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
