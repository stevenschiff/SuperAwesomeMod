package com.steveplays.superawesomemod;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public final class CombatPotionEffectsOverlay {

    private CombatPotionEffectsOverlay() {}

    public static void register() {
        HudRenderCallback.EVENT.register(CombatPotionEffectsOverlay::onHudRender);
    }

    private static void onHudRender(GuiGraphics graphics, DeltaTracker tickCounter) {
        if (!CombatPotionEffectsData.isEnabled()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) return;

        LocalPlayer player = mc.player;
        if (player == null) return;

        int screenWidth = graphics.guiWidth();
        int lineHeight = 11;
        int startY = 52;
        int padding = 4;

        List<MobEffectInstance> effects = new ArrayList<>(player.getActiveEffects());

        // Debug: always show when enabled to confirm rendering works.
        String debugLine = "Time: " + effects.size() + "s";
        int debugWidth = mc.font.width(debugLine);
        graphics.drawString(mc.font, debugLine, screenWidth - debugWidth - padding, startY, 0x55FF55, true);

        int rendered = 1;
        for (MobEffectInstance effect : effects) {
            int duration = effect.getDuration();
            int seconds = duration / 20;

            String name;
            try {
                name = effect.getEffect().value().getDisplayName().getString();
            } catch (Exception e) {
                name = "Effect";
            }

            int amplifier = effect.getAmplifier();
            if (amplifier > 0) {
                name += " " + toRoman(amplifier + 1);
            }

            String line = name + " Time: " + seconds + "s";

            int textWidth = mc.font.width(line);
            int x = screenWidth - textWidth - padding;
            int y = startY + rendered * lineHeight;

            int color = 0xFFFFFF;
            if (seconds < 10) {
                color = 0xFF5555;
            } else if (seconds < 30) {
                color = 0xFFFF55;
            }

            graphics.drawString(mc.font, line, x, y, color, true);
            rendered++;
        }
    }

    private static String toRoman(int num) {
        return switch (num) {
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(num);
        };
    }
}
