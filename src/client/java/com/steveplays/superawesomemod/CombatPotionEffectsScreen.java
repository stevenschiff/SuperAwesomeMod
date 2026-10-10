package com.steveplays.superawesomemod;

import net.minecraft.client.gui.screens.Screen;

public class CombatPotionEffectsScreen extends GabeOptionScreen {

    public CombatPotionEffectsScreen(Screen parent) {
        super(parent, "Potion Effects");
    }

    @Override
    protected void buildOptions() {
        toggle("Enabled", CombatPotionEffectsData::isEnabled, CombatPotionEffectsData::setEnabled);
        info("Compact effect list with timers");
    }
}
