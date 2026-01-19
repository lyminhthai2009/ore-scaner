package com.orescanner.client;

import com.orescanner.scanner.ScanningTask;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class OreScannerClient implements ClientModInitializer {
    private static ScanningTask activeTask = null;

    @Override
    public void onInitializeClient() {
        ModKeyBindings.register();
        HudRenderCallback.EVENT.register(new ScannerHUD());

        // Tick event logic
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (activeTask != null && !activeTask.isComplete()) {
                activeTask.tick(client);
            }
        });

        // Tự động xóa task khi ngắt kết nối khỏi server/world
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            clearTask();
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
