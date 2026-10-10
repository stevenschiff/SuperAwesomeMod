package com.steveplays.superawesomemod;

import net.minecraft.client.gui.screens.Screen;

public class BodyTwistScreen extends GabeOptionScreen {

    public BodyTwistScreen(Screen parent) {
        super(parent, "Body Twist");
    }

    @Override
    protected void buildOptions() {
        toggle("Enabled", BodyTwistData::isEnabled, BodyTwistData::setEnabled);
        slider("Torso lead", BodyTwistData.MIN_LEAD, BodyTwistData.MAX_LEAD,
               BodyTwistData::getLeadLimit, BodyTwistData::setLeadLimit,
               v -> v + (v <= BodyTwistData.VANILLA_LIMIT ? " (vanilla)" : v == 75 ? " (1.8)" : ""));
        toggle("Backwards walking", BodyTwistData::isBackwardsWalking,
               BodyTwistData::setBackwardsWalking);
        toggle("Snap head rotation", BodyTwistData::isSnapHeadRotation,
               BodyTwistData::setSnapHeadRotation);
        info("Visual only - hitboxes unchanged");
    }
}
