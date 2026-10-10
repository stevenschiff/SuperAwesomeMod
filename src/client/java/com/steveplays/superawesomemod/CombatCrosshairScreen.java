package com.steveplays.superawesomemod;

import net.minecraft.client.gui.screens.Screen;

public class CombatCrosshairScreen extends GabeOptionScreen {

    public CombatCrosshairScreen(Screen parent) {
        super(parent, "Combat Crosshair");
    }

    @Override
    protected void buildOptions() {
        toggle("Enabled", CombatCrosshairData::isEnabled, CombatCrosshairData::setEnabled);
        cycle("In range", CombatCrosshairData::getInRangeColorName,
            () -> CombatCrosshairData.setInRangeColor(
                (CombatCrosshairData.getInRangeColor() + 1) % CombatHitboxData.COLOR_NAMES.length));
        cycle("Out of range", CombatCrosshairData::getOutOfRangeColorName,
            () -> CombatCrosshairData.setOutOfRangeColor(
                (CombatCrosshairData.getOutOfRangeColor() + 1) % CombatHitboxData.COLOR_NAMES.length));
        info("Tints the crosshair by attack range");
    }
}
