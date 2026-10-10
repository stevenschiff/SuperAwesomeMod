package com.steveplays.superawesomemod;

import net.minecraft.client.gui.screens.Screen;

public class CombatHitboxScreen extends GabeOptionScreen {

    public CombatHitboxScreen(Screen parent) {
        super(parent, "Combat Hitboxes");
    }

    @Override
    protected void buildOptions() {
        toggle("Enabled", CombatHitboxData::isEnabled, CombatHitboxData::setEnabled);
        toggle("Players only", CombatHitboxData::isPlayersOnly, CombatHitboxData::setPlayersOnly);
        toggle("Full color", CombatHitboxData::isFullColor, CombatHitboxData::setFullColor);
        cycle("In range", CombatHitboxData::getInRangeColorName,
            () -> CombatHitboxData.setInRangeColor(
                (CombatHitboxData.getInRangeColor() + 1) % CombatHitboxData.COLOR_NAMES.length));
        cycle("Out of range", CombatHitboxData::getOutOfRangeColorName,
            () -> CombatHitboxData.setOutOfRangeColor(
                (CombatHitboxData.getOutOfRangeColor() + 1) % CombatHitboxData.COLOR_NAMES.length));
        info("Outlines obey line of sight");
    }
}
