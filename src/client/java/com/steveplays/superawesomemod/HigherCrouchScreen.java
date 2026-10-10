package com.steveplays.superawesomemod;

import net.minecraft.client.gui.screens.Screen;

public class HigherCrouchScreen extends GabeOptionScreen {

    public HigherCrouchScreen(Screen parent) {
        super(parent, "Higher Crouch");
    }

    @Override
    protected void buildOptions() {
        toggle("Enabled", HigherCrouchData::isEnabled, HigherCrouchData::setEnabled);
        info("1.7 style sneak camera height");
    }
}
