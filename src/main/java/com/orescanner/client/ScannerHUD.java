package com.orescanner.client;

import com.orescanner.scanner.ScanResult;
import com.orescanner.scanner.ScanningTask;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

import java.util.List;

/**
 * HUD overlay - Hiển thị:
 * 1. Progress bar khi đang quét
 * 2. Thông tin blocks tìm được
 * 3. Thời gian dự kiến
 */
public class ScannerHUD implements HudRenderCallback {
    
    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        ScanningTask task = OreScannerClient.getActiveTask();
        if (task == null) return;
        
        MinecraftClient client = MinecraftClient.getInstance();
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        
        // Vị trí HUD (góc phải trên)
        int x = screenWidth - 250;
        int y = 10;
        
        // Nền
        context.fill(x - 5, y - 5, x + 245, y + 85, 0xAA000000);
        
        // Tiêu đề
        context.drawText(client.textRenderer, "§e§lORE SCANNER", x, y, 0xFFFFFF, true);
        y += 15;
        
        // Progress
        double progress = task.getProgress();
        int progressPercent = (int) (progress * 100);
        
        String progressText = String.format("§7Tiến độ: §f%d%%", progressPercent);
        context.drawText(client.textRenderer, progressText, x, y, 0xFFFFFF, false);
        y += 12;
        
        // Progress bar
        int barWidth = 230;
        int barHeight = 8;
        
        // Viền
        context.fill(x, y, x + barWidth, y + barHeight, 0xFF555555);
        // Fill
        int fillWidth = (int) (barWidth * progress);
        context.fill(x + 1, y + 1, x + fillWidth - 1, y + barHeight - 1, 0xFF00FF00);
        y += 15;
        
        // Số blocks đã quét
        String blocksText = String.format("§7Đã quét: §f%,d / %,d blocks", 
            task.getScannedBlocks(), 
            task.getTotalBlocks()
        );
        context.drawText(client.textRenderer, blocksText, x, y, 0xFFFFFF, false);
        y += 12;
        
        // Thời gian còn lại
        if (!task.isComplete()) {
            int remaining = task.getEstimatedTimeRemaining();
            String timeText = String.format("§7Thời gian còn: §f%d giây", remaining);
            context.drawText(client.textRenderer, timeText, x, y, 0xFFFFFF, false);
            y += 12;
        }
        
        // Số blocks tìm được
        int foundCount = task.getResults().size();
        String foundText = String.format("§7Tìm thấy: §a%d blocks", foundCount);
        context.drawText(client.textRenderer, foundText, x, y, 0xFFFFFF, false);
        
        // Nếu quét xong, hiển thị chi tiết
        if (task.isComplete()) {
            y += 15;
            context.drawText(client.textRenderer, "§a§lHOÀN THÀNH!", x, y, 0xFFFFFF, true);
            
            // Hiển thị top 3 blocks tìm được gần nhất
            y += 12;
            List<ScanResult> results = task.getResults();
            int displayCount = Math.min(3, results.size());
            
            for (int i = results.size() - displayCount; i < results.size(); i++) {
                ScanResult result = results.get(i);
                String resultText = String.format("§7• %s §fat §f%d, %d, %d", 
                    result.getBlockName().substring(0, Math.min(15, result.getBlockName().length())),
                    result.getPos().getX(),
                    result.getPos().getY(),
                    result.getPos().getZ()
                );
                context.drawText(client.textRenderer, resultText, x, y, 0xFFFFFF, false);
                y += 10;
            }
        }
    }
}