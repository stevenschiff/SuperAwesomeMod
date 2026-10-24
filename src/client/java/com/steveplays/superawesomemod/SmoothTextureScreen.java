package com.steveplays.superawesomemod;

import net.minecraft.client.gui.screens.Screen;

public class SmoothTextureScreen extends GabeOptionScreen {

    public SmoothTextureScreen(Screen parent) {
        super(parent, "Smooth Textures");
    }

    @Override
    protected void buildOptions() {
        toggle("Enabled", SmoothTextureData::isEnabled, SmoothTextureData::setEnabled);
        cycle("Scope", () -> SmoothTextureData.getScope().label(), SmoothTextureData::cycleScope);
        cycle("Mode", () -> SmoothTextureData.getMode().label(), SmoothTextureData::cycleMode);
        toggle("Crisp Pixels", SmoothTextureData::isCrispPixels, SmoothTextureData::setCrispPixels);
        info("Vanilla animates textures 20x a second");
        info("This fills in the frames in between");
        info("Motion-compensated moves, cross-fade dissolves");
        warn("Distant mipmapped surfaces still step");
    }
}
