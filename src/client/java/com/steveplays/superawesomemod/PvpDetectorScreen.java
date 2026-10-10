package com.steveplays.superawesomemod;

import net.minecraft.client.gui.screens.Screen;

public class PvpDetectorScreen extends GabeOptionScreen {

    public PvpDetectorScreen(Screen parent) {
        super(parent, "Cheat Detector");
    }

    @Override
    protected void buildOptions() {
        toggle("Enabled", PvpDetectorData::isEnabled, PvpDetectorData::setEnabled);
        action("Clear history", PvpDetectorTracker::clear);
        info("Flags suspicious reach and hits");
        info("through walls on other players");
    }
}
