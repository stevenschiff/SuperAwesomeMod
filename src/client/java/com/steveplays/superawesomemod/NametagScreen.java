package com.steveplays.superawesomemod;

import net.minecraft.client.gui.screens.Screen;

public class NametagScreen extends GabeOptionScreen {

    public NametagScreen(Screen parent) {
        super(parent, "Nametag");
    }

    @Override
    protected void buildOptions() {
        toggle("Enabled", NametagData::isEnabled, NametagData::setEnabled);
        info("Cleaner nametag rendering");
    }
}
