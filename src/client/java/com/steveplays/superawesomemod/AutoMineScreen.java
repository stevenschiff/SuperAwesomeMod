package com.steveplays.superawesomemod;

import net.minecraft.client.gui.screens.Screen;

public class AutoMineScreen extends GabeOptionScreen {

    public AutoMineScreen(Screen parent) {
        super(parent, "Auto Miner");
    }

    @Override
    protected void buildOptions() {
        toggle("Enabled", AutoMineData::isEnabled, AutoMineData::setEnabled);
        slider("Search radius", AutoMineData.MIN_RADIUS, AutoMineData.MAX_RADIUS,
               AutoMineData::getSearchRadius, AutoMineData::setSearchRadius, v -> v + " blocks");
        action("Clear marked block", () -> {
            AutoMineData.setTarget(null);
            AutoMineData.stop("Idle");
        });
        info("Marked: " + AutoMineData.getTargetName());
        info("Status: " + AutoMineData.getStatus());
        info("V marks the block you look at");
        info("B starts and stops mining");
        warn("Walks open routes only - never digs");
        warn("Stops the moment you take damage");
    }
}
