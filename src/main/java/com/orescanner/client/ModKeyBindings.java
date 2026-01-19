package com.orescanner.client;

import com.orescanner.client.gui.ScanConfigScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ModKeyBindings {
    private static KeyBinding openConfigKey;

    public static void register() {
        openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.orescanner.openconfig",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            "category.orescanner.main"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openConfigKey.wasPressed()) {
                long handle = client.getWindow().getHandle();
                boolean shift = InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT_SHIFT) || 
                                InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_RIGHT_SHIFT);

                if (shift && client.currentScreen == null) {
                    client.setScreen(new ScanConfigScreen());
                }
            }
        });
    }
}
