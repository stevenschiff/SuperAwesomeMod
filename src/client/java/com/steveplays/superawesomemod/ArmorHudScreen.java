package com.steveplays.superawesomemod;

import net.minecraft.client.gui.screens.Screen;

public class ArmorHudScreen extends GabeOptionScreen {

    public ArmorHudScreen(Screen parent) {
        super(parent, "Armor HUD");
    }

    @Override
    protected void buildOptions() {
        toggle("Enabled", ArmorHudData::isEnabled, ArmorHudData::setEnabled);
        // Scales are floats; the slider works in tenths so it stays a whole number.
        slider("Icon size", 5, 50,
               () -> Math.round(ArmorHudData.getScale() * 10f),
               v -> ArmorHudData.setScale(v / 10f),
               v -> String.format("%.1fx", v / 10f));
        slider("Text size", 5, 30,
               () -> Math.round(ArmorHudData.getTextScale() * 10f),
               v -> ArmorHudData.setTextScale(v / 10f),
               v -> String.format("%.1fx", v / 10f));
        slider("Bar height", 0, 30,
               ArmorHudData::getDurabilityHeight, ArmorHudData::setDurabilityHeight,
               String::valueOf);
    }
}
