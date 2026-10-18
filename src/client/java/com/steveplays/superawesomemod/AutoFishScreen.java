package com.steveplays.superawesomemod;

import net.minecraft.client.gui.screens.Screen;

public class AutoFishScreen extends GabeOptionScreen {

    public AutoFishScreen(Screen parent) {
        super(parent, "Auto Fishing");
    }

    @Override
    protected void buildOptions() {
        toggle("Enabled", AutoFishData::isEnabled, AutoFishData::setEnabled);
        slider("Keep durability", AutoFishData.MIN_RESERVE, AutoFishData.MAX_RESERVE,
               AutoFishData::getDurabilityReserve, AutoFishData::setDurabilityReserve,
               String::valueOf);
        info("Status: " + AutoFishData.getStatus());
        info("Hold a rod and look at water");
        info("Stops before the rod breaks");
    }
}
