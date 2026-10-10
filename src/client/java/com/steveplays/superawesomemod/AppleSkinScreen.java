package com.steveplays.superawesomemod;

import net.minecraft.client.gui.screens.Screen;

public class AppleSkinScreen extends GabeOptionScreen {

    public AppleSkinScreen(Screen parent) {
        super(parent, "AppleSkin");
    }

    @Override
    protected void buildOptions() {
        toggle("Enabled", AppleSkinData::isEnabled, AppleSkinData::setEnabled);
        info("Shows saturation and food values");
    }
}
