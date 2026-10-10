package com.steveplays.superawesomemod;

import net.minecraft.client.gui.screens.Screen;

public class CpsScreen extends GabeOptionScreen {

    public CpsScreen(Screen parent) {
        super(parent, "CPS Counter");
    }

    @Override
    protected void buildOptions() {
        toggle("Enabled", CpsData::isEnabled, CpsData::setEnabled);
        slider("Size", 1, 10, CpsData::getScale, CpsData::setScale, String::valueOf);
        slider("Offset", 0, 100, CpsData::getOffset, CpsData::setOffset, String::valueOf);
    }
}
