package com.steveplays.superawesomemod;

import net.minecraft.client.gui.screens.Screen;

public class ShulkerTooltipScreen extends GabeOptionScreen {

    public ShulkerTooltipScreen(Screen parent) {
        super(parent, "Shulker Tooltips");
    }

    @Override
    protected void buildOptions() {
        toggle("Enabled", ShulkerTooltipData::isEnabled, ShulkerTooltipData::setEnabled);
        slider("Size", ShulkerTooltipData.MIN_SCALE, ShulkerTooltipData.MAX_SCALE,
               ShulkerTooltipData::getScale, ShulkerTooltipData::setScale, String::valueOf);
        info("Previews contents on hover");
    }
}
