package com.steveplays.superawesomemod;

import net.minecraft.client.gui.screens.Screen;

public class ItemPhysicsScreen extends GabeOptionScreen {

    public ItemPhysicsScreen(Screen parent) {
        super(parent, "Item Physics");
    }

    @Override
    protected void buildOptions() {
        toggle("Enabled", ItemPhysicsData::isEnabled, ItemPhysicsData::setEnabled);
        info("Dropped items lie flat on the ground");
    }
}
