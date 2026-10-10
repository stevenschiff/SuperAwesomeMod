package com.steveplays.superawesomemod;

import net.minecraft.client.gui.screens.Screen;

public class OldPvpScreen extends GabeOptionScreen {

    public OldPvpScreen(Screen parent) {
        super(parent, "1.7 Animations");
    }

    @Override
    protected void buildOptions() {
        toggle("Sword blocking", OldPvpData::isBlockingEnabled, OldPvpData::setBlockingEnabled);
        toggle("Swing while using", OldPvpData::isSwingWhileUsingEnabled,
               OldPvpData::setSwingWhileUsingEnabled);
        info("Visual only - no gameplay change");
    }
}
