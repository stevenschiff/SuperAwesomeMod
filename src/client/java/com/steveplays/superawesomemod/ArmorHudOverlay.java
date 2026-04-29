package com.steveplays.superawesomemod;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

public final class ArmorHudOverlay {

    private static final Identifier ARMOR_FULL_SPRITE  = Identifier.withDefaultNamespace("hud/armor_full");
    private static final Identifier ARMOR_HALF_SPRITE  = Identifier.withDefaultNamespace("hud/armor_half");
    private static final Identifier ARMOR_EMPTY_SPRITE = Identifier.withDefaultNamespace("hud/armor_empty");

    private static final Identifier ID = Identifier.fromNamespaceAndPath("superawesomemod", "armor_hud");

    private ArmorHudOverlay() {}

    public static void register() {
        HudElementRegistry.attachElementAfter(VanillaHudElements.ARMOR_BAR, ID, (graphics, tickCounter) -> {
            if (!ArmorHudData.isEnabled()) return;

            Minecraft mc = Minecraft.getInstance();
            if (mc.options.hideGui) return;

            LocalPlayer player = mc.player;
            if (player == null) return;

            // Match vanilla: armor bar is hidden in spectator/creative.
            if (mc.gameMode != null) {
                GameType type = mc.gameMode.getPlayerMode();
                if (type == GameType.SPECTATOR || type == GameType.CREATIVE) return;
            }

            int armor = player.getArmorValue();
            if (armor <= 0) return;

            renderArmor(graphics, player, armor);
        });
    }

    private static void renderArmor(GuiGraphics graphics, Player player, int armor) {
        int width  = graphics.guiWidth();
        int height = graphics.guiHeight();
        int leftSide = width / 2 - 91;
        // Vanilla stacks status bars in 10px rows above the hotbar; armor sits one row above health.
        int y = height - 39 - 10;

        for (int i = 0; i < 10; i++) {
            int x = leftSide + i * 8;
            Identifier sprite;
            if (i * 2 + 1 < armor) {
                sprite = ARMOR_FULL_SPRITE;
            } else if (i * 2 + 1 == armor) {
                sprite = ARMOR_HALF_SPRITE;
            } else {
                sprite = ARMOR_EMPTY_SPRITE;
            }
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, 9, 9);
        }
    }
}
