package com.steveplays.superawesomemod;

import net.minecraft.client.gui.screens.Screen;

public class RenderDistanceScreen extends GabeOptionScreen {

    public RenderDistanceScreen(Screen parent) {
        super(parent, "Render Distance");
    }

    @Override
    protected void buildOptions() {
        toggle("Enabled", RenderDistanceData::isEnabled, RenderDistanceData::setEnabled);
        slider("Distance", RenderDistanceData.MIN_DISTANCE, RenderDistanceData.MAX_DISTANCE,
               RenderDistanceData::getDistance, RenderDistanceData::setDistance,
               v -> v + " chunks");
        info(RenderDistanceData.isSeedSet()
            ? "Seed detected for LOD terrain"
            : "Seed auto-detects in singleplayer");
        warn("Past the server's distance you see");
        warn("generated terrain, not real chunks");
    }
}
