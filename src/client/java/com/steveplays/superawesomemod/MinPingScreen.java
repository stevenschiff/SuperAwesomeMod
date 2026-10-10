package com.steveplays.superawesomemod;

import net.minecraft.client.gui.screens.Screen;

public class MinPingScreen extends GabeOptionScreen {

    public MinPingScreen(Screen parent) {
        super(parent, "Min Ping");
    }

    @Override
    protected void buildOptions() {
        toggle("Enabled", MinPingData::isEnabled, MinPingData::setEnabled);
        slider("Minimum", MinPingData.MIN_PING, MinPingData.MAX_PING,
               MinPingData::getMinPingMs, MinPingData::setMinPingMs, v -> v + " ms");
        info("Holds packets to a floor latency");
    }
}
