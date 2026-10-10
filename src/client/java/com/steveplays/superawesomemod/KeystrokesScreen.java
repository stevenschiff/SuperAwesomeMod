package com.steveplays.superawesomemod;

import net.minecraft.client.gui.screens.Screen;

public class KeystrokesScreen extends GabeOptionScreen {

    public KeystrokesScreen(Screen parent) {
        super(parent, "Keystrokes");
    }

    @Override
    protected void buildOptions() {
        toggle("Enabled", KeystrokesData::isEnabled, KeystrokesData::setEnabled);
        slider("Size", 1, 10, KeystrokesData::getScale, KeystrokesData::setScale, String::valueOf);
        cycle("Corner", KeystrokesData::getCornerName,
            () -> KeystrokesData.setCorner((KeystrokesData.getCorner() + 1) % 4));
    }
}
