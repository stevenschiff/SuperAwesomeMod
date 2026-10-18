package com.steveplays.superawesomemod;

import net.minecraft.client.gui.screens.Screen;

public class ChatMacroScreen extends GabeOptionScreen {

    public ChatMacroScreen(Screen parent) {
        super(parent, "Chat Macros");
    }

    @Override
    protected void buildOptions() {
        toggle("Enabled", ChatMacroData::isEnabled, ChatMacroData::setEnabled);
        toggle("Repeat While Held", ChatMacroData::isRepeatWhileHeld,
               ChatMacroData::setRepeatWhileHeld);
        for (int i = 0; i < ChatMacroData.SLOTS; i++) {
            final int slot = i;
            text("Macro " + (i + 1),
                 () -> ChatMacroData.getMessage(slot),
                 v -> ChatMacroData.setMessage(slot, v));
        }
        slider("Cooldown", ChatMacroData.MIN_COOLDOWN_MS, ChatMacroData.MAX_COOLDOWN_MS,
               ChatMacroData::getCooldownMs, ChatMacroData::setCooldownMs, v -> v + " ms");
        info("Click a macro, type, press Enter");
        info("Lines starting with / run as commands");
        info("Bind keys in Options > Controls");
        info("Repeat off: one send per press");
        info("Repeat on: sends every cooldown while held");
        warn("Servers kick for chat spam under ~1/sec");
    }
}
