package com.orescanner.client;

import com.orescanner.scanner.ScanningTask;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

/**
 * Client-side initializer - Đã sửa lỗi tự động xóa task
 */
public class OreScannerClient implements ClientModInitializer {
    private static ScanningTask activeTask = null;
    private static int displayTicks = 0; // Bộ đếm để ẩn HUD sau khi xong

    @Override
    public void onInitializeClient() {
        // Đăng ký phím tắt Shift+P
        ModKeyBindings.register();

        // Đăng ký HUD overlay
        HudRenderCallback.EVENT.register(new ScannerHUD());

        // Reset task khi thoát game (Server hoặc Singleplayer)
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            clearTask();
        });

        // Tick event - Chạy scanner và xử lý tự động ẩn HUD
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (activeTask != null) {
                if (!activeTask.isComplete()) {
                    activeTask.tick(client);
                    displayTicks = 100; // Giữ HUD hiện thêm 5 giây (100 ticks) sau khi hoàn thành
                } else {
                    // Nếu đã hoàn thành, bắt đầu đếm ngược để ẩn
                    if (displayTicks > 0) {
                        displayTicks--;
                    } else {
                        activeTask = null; // Tự động xóa task để ẩn HUD
                    }
                }
            }
        });
    }

    public static void startScan(ScanningTask task) {
        activeTask = task;
        displayTicks = 100;
    }

    public static ScanningTask getActiveTask() {
        return activeTask;
    }

    public static void clearTask() {
        activeTask = null;
        displayTicks = 0;
    }
}
