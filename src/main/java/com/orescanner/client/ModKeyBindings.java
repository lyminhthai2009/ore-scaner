package com.orescanner.client;

import com.orescanner.client.gui.ScanConfigScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * Quản lý phím tắt của mod
 * Shift + P để mở menu cấu hình
 */
public class ModKeyBindings {
    private static KeyBinding openConfigKey;

    public static void register() {
        // Đăng ký phím P (Shift được check trong tick event)
        openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.orescanner.openconfig",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            "category.orescanner.main"
        ));

        // Tick event để check Shift + P
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openConfigKey.wasPressed()) {
                // Check nếu đang giữ Shift
                boolean shiftPressed = InputUtil.isKeyPressed(
                    client.getWindow().getHandle(), 
                    GLFW.GLFW_KEY_LEFT_SHIFT
                ) || InputUtil.isKeyPressed(
                    client.getWindow().getHandle(), 
                    GLFW.GLFW_KEY_RIGHT_SHIFT
                );

                if (shiftPressed && client.currentScreen == null) {
                    client.setScreen(new ScanConfigScreen());
                }
            }
        });
    }
}