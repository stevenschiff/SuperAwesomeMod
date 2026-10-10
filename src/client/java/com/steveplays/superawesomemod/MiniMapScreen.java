package com.steveplays.superawesomemod;

import net.minecraft.client.gui.screens.Screen;

public class MiniMapScreen extends GabeOptionScreen {

    public MiniMapScreen(Screen parent) {
        super(parent, "Mini Map");
    }

    @Override
    protected void buildOptions() {
        toggle("Enabled", MiniMapData::isEnabled, MiniMapData::setEnabled);
        toggle("Corner map", MiniMapData::isHudVisible, MiniMapData::setHudVisible);
        slider("Size", 64, 256, MiniMapData::getMinimapSize, MiniMapData::setMinimapSize,
               v -> v + "px");
        cycle("Corner", MiniMapData::getCornerName,
            () -> MiniMapData.setCorner((MiniMapData.getCorner() + 1) % 4));
        toggle("Marked waypoints only", MiniMapData::isSpecificWaypoints,
               MiniMapData::setSpecificWaypoints);
        action("Manage waypoints",
            () -> this.minecraft.setScreen(new MiniMapWaypointListScreen(this)));
        info("Press C for the full map");
    }
}
