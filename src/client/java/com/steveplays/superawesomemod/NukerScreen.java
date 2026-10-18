package com.steveplays.superawesomemod;

import net.minecraft.client.gui.screens.Screen;

public class NukerScreen extends GabeOptionScreen {

    public NukerScreen(Screen parent) {
        super(parent, "Nuker");
    }

    @Override
    protected void buildOptions() {
        toggle("Enabled", NukerData::isEnabled, NukerData::setEnabled);
        slider("Radius", NukerData.MIN_RADIUS, NukerData.MAX_RADIUS,
               NukerData::getRadius, NukerData::setRadius, v -> v + " blocks");
        toggle("Instant Blocks Only", NukerData::isInstantOnly, NukerData::setInstantOnly);
        info("Instant for grass, crops, torches, leaves w/ shears");
        info("With instant-only off, hard blocks go one at a time");
        warn("Stone and ore are never instant - the server times those");
    }
}
