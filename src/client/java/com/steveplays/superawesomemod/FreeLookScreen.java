package com.steveplays.superawesomemod;

import net.minecraft.client.gui.screens.Screen;

public class FreeLookScreen extends GabeOptionScreen {

    public FreeLookScreen(Screen parent) {
        super(parent, "Free Look");
    }

    @Override
    protected void buildOptions() {
        toggle("Enabled", FreeLookData::isEnabled, FreeLookData::setEnabled);
        info("Active in third person only (F5)");
        info("Look around without turning");
    }
}
