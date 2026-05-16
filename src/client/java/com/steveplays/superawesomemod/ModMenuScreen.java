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

        // 19 buttons centered vertically around cy: rows 0..18 of `gap`.
        int row0 = cy - (18 * gap) / 2;

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

        this.addRenderableWidget(Button.builder(
            Component.literal("Combat Hitboxes"),
            btn -> this.minecraft.setScreen(new CombatHitboxScreen(this))
        ).bounds(cx - btnW / 2, row0 + gap * 5, btnW, btnH).build());

        this.addRenderableWidget(Button.builder(
            Component.literal("Combat Crosshair"),
            btn -> this.minecraft.setScreen(new CombatCrosshairScreen(this))
        ).bounds(cx - btnW / 2, row0 + gap * 6, btnW, btnH).build());

        this.addRenderableWidget(Button.builder(
            Component.literal("Combat Potion Effects"),
            btn -> this.minecraft.setScreen(new CombatPotionEffectsScreen(this))
        ).bounds(cx - btnW / 2, row0 + gap * 7, btnW, btnH).build());

        this.addRenderableWidget(Button.builder(
            Component.literal("PvP Cheat Detector"),
            btn -> this.minecraft.setScreen(new PvpDetectorScreen(this))
        ).bounds(cx - btnW / 2, row0 + gap * 8, btnW, btnH).build());

        this.addRenderableWidget(Button.builder(
            Component.literal("Autoclicker"),
            btn -> this.minecraft.setScreen(new AutoclickerScreen(this))
        ).bounds(cx - btnW / 2, row0 + gap * 9, btnW, btnH).build());

        this.addRenderableWidget(Button.builder(
            Component.literal("Freecam"),
            btn -> this.minecraft.setScreen(new FreecamScreen(this))
        ).bounds(cx - btnW / 2, row0 + gap * 10, btnW, btnH).build());

        this.addRenderableWidget(Button.builder(
            Component.literal("AppleSkin"),
            btn -> this.minecraft.setScreen(new AppleSkinScreen(this))
        ).bounds(cx - btnW / 2, row0 + gap * 11, btnW, btnH).build());

        this.addRenderableWidget(Button.builder(
            Component.literal("Shulker Tooltips"),
            btn -> this.minecraft.setScreen(new ShulkerTooltipScreen(this))
        ).bounds(cx - btnW / 2, row0 + gap * 12, btnW, btnH).build());

        this.addRenderableWidget(Button.builder(
            Component.literal("Render Distance"),
            btn -> this.minecraft.setScreen(new RenderDistanceScreen(this))
        ).bounds(cx - btnW / 2, row0 + gap * 13, btnW, btnH).build());

        this.addRenderableWidget(Button.builder(
            Component.literal("Item Physics"),
            btn -> this.minecraft.setScreen(new ItemPhysicsScreen(this))
        ).bounds(cx - btnW / 2, row0 + gap * 14, btnW, btnH).build());

        this.addRenderableWidget(Button.builder(
            Component.literal("1.7 PvP Animations"),
            btn -> this.minecraft.setScreen(new OldPvpScreen(this))
        ).bounds(cx - btnW / 2, row0 + gap * 15, btnW, btnH).build());

        this.addRenderableWidget(Button.builder(
            Component.literal("X-Ray"),
            btn -> this.minecraft.setScreen(new XrayScreen(this))
        ).bounds(cx - btnW / 2, row0 + gap * 16, btnW, btnH).build());

        this.addRenderableWidget(Button.builder(
            Component.literal("Mini Map"),
            btn -> this.minecraft.setScreen(new MiniMapScreen(this))
        ).bounds(cx - btnW / 2, row0 + gap * 17, btnW, btnH).build());

        // --- Close ---
        this.addRenderableWidget(Button.builder(
            Component.literal("Close"),
            btn -> this.onClose()
        ).bounds(cx - btnW / 2, row0 + gap * 18, btnW, btnH).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);
        graphics.drawCenteredString(this.font, this.title,
            this.width / 2, this.height / 2 - 132, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
