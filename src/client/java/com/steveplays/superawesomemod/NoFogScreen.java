package com.steveplays.superawesomemod;

import net.minecraft.client.gui.screens.Screen;

public class NoFogScreen extends GabeOptionScreen {

    public NoFogScreen(Screen parent) {
        super(parent, "No Fog");
    }

    @Override
    protected void buildOptions() {
        toggle("Enabled", NoFogData::isEnabled, NoFogData::setEnabled);
        info("Removes distance, water and lava fog");
    }
}
