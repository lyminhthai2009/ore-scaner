package com.orescanner.client;

import com.orescanner.scanner.ScanningTask;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

/**
 * Client-side initializer - Đăng ký keybindings, HUD, và tick events
 */
public class OreScannerClient implements ClientModInitializer {
    private static ScanningTask activeTask = null;

    @Override
    public void onInitializeClient() {
        // Đăng ký phím tắt Shift+P
        ModKeyBindings.register();

        // Đăng ký HUD overlay (hiển thị thông tin quét)
        HudRenderCallback.EVENT.register(new ScannerHUD());

        // Tick event - Chạy scanner mỗi tick (chống lag)
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (activeTask != null && !activeTask.isComplete()) {
                activeTask.tick(client);
            }
        });
    }

    public static void startScan(ScanningTask task) {
        activeTask = task;
    }

    public static ScanningTask getActiveTask() {
        return activeTask;
    }

    public static void clearTask() {
        activeTask = null;
    }
}