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
    public static KeyMapping freeLook;

    public static void register() {
        openMenu = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.superawesomemod.open_menu",    // translation key (shown in Controls list)
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,                    // default key: G  (rebindable)
            CATEGORY
        ));
        freeLook = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.superawesomemod.freelook",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,                    // default key: V (hold to free-look)
            CATEGORY
        ));
    }
}
