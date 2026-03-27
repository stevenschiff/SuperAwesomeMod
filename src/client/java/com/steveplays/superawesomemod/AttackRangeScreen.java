package com.steveplays.superawesomemod;

import com.steveplays.superawesomemod.network.AttackRangePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class AttackRangeScreen extends Screen {

    private final Screen parent;
    private EditBox valueField;
    private Component errorMessage = Component.empty();

    public AttackRangeScreen(Screen parent) {
        super(Component.literal("Attack Range"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int cy = this.height / 2;

        this.valueField = new EditBox(this.font,
            cx - 50, cy - 10, 100, 20,
            Component.literal("Range"));
        this.valueField.setValue(String.valueOf((float) getCurrentRange()));
        this.valueField.setResponder(v -> this.errorMessage = Component.empty());
        this.addRenderableWidget(this.valueField);

        // Apply
        this.addRenderableWidget(Button.builder(
            Component.literal("Apply"),
            btn -> this.apply()
        ).bounds(cx - 102, cy + 16, 98, 20).build());

        // Reset to vanilla default
        this.addRenderableWidget(Button.builder(
            Component.literal("Reset (" + AttackRangePayload.DEFAULT + ")"),
            btn -> {
                this.sendRangePacket(AttackRangePayload.DEFAULT);
                this.minecraft.setScreen(this.parent);
            }
        ).bounds(cx + 4, cy + 16, 98, 20).build());

        // Back
        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(cx - 50, cy + 42, 100, 20).build());
    }

    private float getCurrentRange() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            return PlayerAttackRangeData.getRange(mc.player.getUUID());
        }
        return AttackRangePayload.DEFAULT;
    }

    private void apply() {
        String raw = this.valueField.getValue().trim();
        try {
            float value = Float.parseFloat(raw);
            if (value < AttackRangePayload.MIN || value > AttackRangePayload.MAX) {
                this.errorMessage = Component.literal(
                    "Value must be " + AttackRangePayload.MIN + " – " + AttackRangePayload.MAX);
                return;
            }
            this.sendRangePacket(value);
            this.minecraft.setScreen(this.parent);
        } catch (NumberFormatException e) {
            this.errorMessage = Component.literal("Enter a valid number");
        }
    }

    private void sendRangePacket(float value) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            // Write to the shared ConcurrentHashMap so the server tick hook picks it up
            // immediately in singleplayer (same JVM — no packet round-trip needed).
            PlayerAttackRangeData.setRange(mc.player.getUUID(), value);

            // Also apply client-side attribute for immediate local feedback.
            AttributeInstance attr = mc.player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
            if (attr != null) attr.setBaseValue(value);
        }
        // Also send the packet so dedicated servers (separate JVM) are updated.
        ClientPlayNetworking.send(new AttackRangePayload(value));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);

        int cx = this.width / 2;
        int cy = this.height / 2;

        graphics.drawCenteredString(this.font, this.title, cx, cy - 60, 0xFFFFFF);
        graphics.drawCenteredString(this.font,
            Component.literal("Range in blocks  (" + AttackRangePayload.MIN + " – " + AttackRangePayload.MAX + ")"
                + "  |  Vanilla: " + AttackRangePayload.DEFAULT),
            cx, cy - 28, 0xAAAAAA);

        if (!this.errorMessage.getString().isEmpty()) {
            graphics.drawCenteredString(this.font, this.errorMessage, cx, cy + 70, 0xFF5555);
        }

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
