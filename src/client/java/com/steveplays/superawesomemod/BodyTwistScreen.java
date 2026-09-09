package com.steveplays.superawesomemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class BodyTwistScreen extends Screen {

    private final Screen parent;

    public BodyTwistScreen(Screen parent) {
        super(Component.literal("Body Twist"));
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
                BodyTwistData.setEnabled(!BodyTwistData.isEnabled());
                btn.setMessage(toggleLabel());
            }
        ).bounds(cx - btnW / 2, cy - 52, btnW, btnH).build());

        this.addRenderableWidget(new LeadSlider(cx - btnW / 2, cy - 28, btnW, btnH,
                BodyTwistData.getLeadLimit()));

        this.addRenderableWidget(Button.builder(
            sub("Backwards walking", BodyTwistData.isBackwardsWalking()),
            btn -> {
                BodyTwistData.setBackwardsWalking(!BodyTwistData.isBackwardsWalking());
                btn.setMessage(sub("Backwards walking", BodyTwistData.isBackwardsWalking()));
            }
        ).bounds(cx - btnW / 2, cy - 4, btnW, btnH).build());

        this.addRenderableWidget(Button.builder(
            sub("Snap head rotation", BodyTwistData.isSnapHeadRotation()),
            btn -> {
                BodyTwistData.setSnapHeadRotation(!BodyTwistData.isSnapHeadRotation());
                btn.setMessage(sub("Snap head rotation", BodyTwistData.isSnapHeadRotation()));
            }
        ).bounds(cx - btnW / 2, cy + 20, btnW, btnH).build());

        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(cx - 50, cy + 44, 100, btnH).build());
    }

    private Component toggleLabel() {
        return Component.literal(BodyTwistData.isEnabled() ? "Disable Body Twist" : "Enable Body Twist");
    }

    private static Component sub(String name, boolean on) {
        return Component.literal(name + ": " + (on ? "On" : "Off"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {

        int cx = this.width / 2;
        int cy = this.height / 2;

        graphics.drawCenteredString(this.font, this.title, cx, cy - 82, 0xFFFFFF);

        boolean on = BodyTwistData.isEnabled();
        graphics.drawCenteredString(this.font,
            Component.literal("Status: " + (on ? "Enabled" : "Disabled")),
            cx, cy - 70, on ? 0x55FF55 : 0xFF5555);

        graphics.drawCenteredString(this.font,
            Component.literal("1.7 style waist - the torso leads, the legs follow late"),
            cx, cy + 70, 0xAAAAAA);

        graphics.drawCenteredString(this.font,
            Component.literal("Backwards walking: no 180 flip, you walk backwards properly"),
            cx, cy + 82, 0xAAAAAA);

        graphics.drawCenteredString(this.font,
            Component.literal("Applies to every player  |  hitboxes stay accurate"),
            cx, cy + 94, 0xFFAA00);

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static final class LeadSlider extends AbstractSliderButton {
        LeadSlider(int x, int y, int w, int h, int initial) {
            super(x, y, w, h, Component.empty(), normalize(initial));
            this.updateMessage();
        }

        private static double normalize(int v) {
            return (double) (v - BodyTwistData.MIN_LEAD)
                 / (BodyTwistData.MAX_LEAD - BodyTwistData.MIN_LEAD);
        }

        private int denormalize() {
            return (int) Math.round(this.value
                * (BodyTwistData.MAX_LEAD - BodyTwistData.MIN_LEAD) + BodyTwistData.MIN_LEAD);
        }

        @Override
        protected void updateMessage() {
            int v = denormalize();
            String note = v <= BodyTwistData.VANILLA_LIMIT ? " (vanilla)"
                        : v == 75 ? " (1.8)" : "";
            this.setMessage(Component.literal("Torso lead: " + v + note));
        }

        @Override
        protected void applyValue() {
            BodyTwistData.setLeadLimit(denormalize());
        }
    }
}
