package com.steveplays.superawesomemod;

import net.minecraft.client.gui.screens.Screen;

public class ShieldScreen extends GabeOptionScreen {

    public ShieldScreen(Screen parent) {
        super(parent, "Shield");
    }

    @Override
    protected void buildOptions() {
        toggle("Enabled", ShieldData::isEnabled, ShieldData::setEnabled);
        slider("Transparency", ShieldData.MIN_TRANSPARENCY, ShieldData.MAX_TRANSPARENCY,
               ShieldData::getTransparency, ShieldData::setTransparency, v -> v + "%");
        slider("When used", ShieldData.MIN_OFFSET, ShieldData.MAX_OFFSET,
               ShieldData::getUsedOffset, ShieldData::setUsedOffset,
               v -> (v > 0 ? "+" : "") + v);
        slider("When not used", ShieldData.MIN_OFFSET, ShieldData.MAX_OFFSET,
               ShieldData::getNotUsedOffset, ShieldData::setNotUsedOffset,
               v -> (v > 0 ? "+" : "") + v);
        warn("Banner patterns stay solid");
    }
}
