package com.steveplays.superawesomemod;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class ModKeybindings {

    // Custom category shown in Options → Controls
    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
        Identifier.fromNamespaceAndPath("superawesomemod", "mod_keys")
    );

    public static KeyMapping openMenu;
    public static KeyMapping zoom;
    public static KeyMapping miniMapToggle;
    public static KeyMapping schematicLayerUp;
    public static KeyMapping schematicLayerDown;
    public static KeyMapping schematicToggle;
    public static KeyMapping autoMineMark;
    public static KeyMapping autoMineToggle;
    /** One key per macro slot, unbound by default so nothing is stolen from vanilla. */
    public static KeyMapping[] chatMacros;

    public static void register() {
        openMenu = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.superawesomemod.open_menu",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            CATEGORY
        ));
        zoom = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.superawesomemod.zoom",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_X,
            CATEGORY
        ));
        miniMapToggle = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.superawesomemod.minimap_toggle",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            CATEGORY
        ));
        schematicLayerUp = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.superawesomemod.schematic_layer_up",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_PAGE_UP,
            CATEGORY
        ));
        schematicLayerDown = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.superawesomemod.schematic_layer_down",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_PAGE_DOWN,
            CATEGORY
        ));
        schematicToggle = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.superawesomemod.schematic_toggle",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            CATEGORY
        ));
        // Inert unless Auto Miner is switched on in the menu, so V and B stay free.
        autoMineMark = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.superawesomemod.automine_mark",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            CATEGORY
        ));
        autoMineToggle = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.superawesomemod.automine_toggle",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            CATEGORY
        ));

        chatMacros = new KeyMapping[ChatMacroData.SLOTS];
        for (int i = 0; i < ChatMacroData.SLOTS; i++) {
            chatMacros[i] = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.superawesomemod.chat_macro_" + (i + 1),
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                CATEGORY
            ));
        }
    }
}
