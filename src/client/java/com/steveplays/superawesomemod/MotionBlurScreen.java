package com.steveplays.superawesomemod;

import net.minecraft.client.gui.screens.Screen;

public class MotionBlurScreen extends GabeOptionScreen {

    public MotionBlurScreen(Screen parent) {
        super(parent, "Motion Blur");
    }

    @Override
    protected void buildOptions() {
        toggle("Enabled", MotionBlurData::isEnabled, MotionBlurData::setEnabled);
        slider("Strength", MotionBlurData.MIN_STRENGTH, MotionBlurData.MAX_STRENGTH,
               MotionBlurData::getStrength, MotionBlurData::setStrength, String::valueOf);
    }
}
