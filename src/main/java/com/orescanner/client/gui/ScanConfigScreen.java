package com.orescanner.client.gui;

import com.orescanner.client.OreScannerClient;
import com.orescanner.scanner.BlockTargets;
import com.orescanner.scanner.ScanningTask;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

/**
 * Menu cấu hình scan
 * Có sliders, checkboxes, input fields
 */
public class ScanConfigScreen extends Screen {
    private final BlockTargets targets = new BlockTargets();
    
    // Sliders
    private SliderWidget chunkRadiusSlider;
    private SliderWidget blocksPerSecSlider;
    
    // Text fields
    private TextFieldWidget minYField;
    private TextFieldWidget maxYField;
    
    // Buttons
    private ButtonWidget startButton;
    private int scrollOffset = 0;
    
    public ScanConfigScreen() {
        super(Text.literal("Ore Scanner Configuration"));
    }
    
    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = 30;
        
        // === SLIDERS ===
        
        // Chunk Radius (1-12)
        this.chunkRadiusSlider = new SliderWidget(
            centerX - 150, startY, 300, 20,
            Text.literal("Bán kính: "), Text.literal(" chunks"),
            1, 12, 5  // ← THIẾU this.textRenderer
        );
        this.addDrawableChild(chunkRadiusSlider);
        startY += 30;
        
        // Blocks per Second (1,000 - 500,000)
        this.blocksPerSecSlider = new SliderWidget(
            centerX - 150, startY, 300, 20,
            Text.literal("Tốc độ: "), Text.literal(" blocks/s"),
            1000, 500000, 50000  // ← THIẾU this.textRenderer
        );
        this.addDrawableChild(blocksPerSecSlider);
        startY += 40;
        
        // === Y LEVEL INPUTS ===
        
        // Min Y
        this.minYField = new TextFieldWidget(
            this.textRenderer, 
            centerX - 150, startY, 140, 20,
            Text.literal("Min Y")
        );
        this.minYField.setText("-64");
        this.minYField.setMaxLength(5);
        this.addDrawableChild(minYField);
        
        // Max Y
        this.maxYField = new TextFieldWidget(
            this.textRenderer,
            centerX + 10, startY, 140, 20,
            Text.literal("Max Y")
        );
        this.maxYField.setText("320");
        this.maxYField.setMaxLength(5);
        this.addDrawableChild(maxYField);
        
        startY += 30;
        
        // === QUICK SELECT BUTTONS ===
        
        ButtonWidget selectAllBtn = ButtonWidget.builder(
            Text.literal("Chọn tất cả"),
            btn -> targets.selectAll()
        ).dimensions(centerX - 150, startY, 95, 20).build();
        this.addDrawableChild(selectAllBtn);
        
        ButtonWidget clearAllBtn = ButtonWidget.builder(
            Text.literal("Bỏ chọn tất cả"),
            btn -> targets.clearAll()
        ).dimensions(centerX - 50, startY, 95, 20).build();
        this.addDrawableChild(clearAllBtn);
        
        ButtonWidget presetsBtn = ButtonWidget.builder(
            Text.literal("Diamonds Only"),
            btn -> {
                targets.clearAll();
                targets.toggleBlock(net.minecraft.block.Blocks.DIAMOND_ORE);
                targets.toggleBlock(net.minecraft.block.Blocks.DEEPSLATE_DIAMOND_ORE);
            }
        ).dimensions(centerX + 50, startY, 100, 20).build();
        this.addDrawableChild(presetsBtn);
        
        startY += 30;
        
        // === START BUTTON ===
        
        this.startButton = ButtonWidget.builder(
            Text.literal("§a§lBẮT ĐẦU QUÉT"),
            btn -> startScan()
        ).dimensions(centerX - 100, this.height - 40, 200, 30).build();
        this.addDrawableChild(startButton);
        
        // === CHECKBOXES (sẽ render trong render method) ===
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Background
        this.renderBackground(context, mouseX, mouseY, delta);
        
        // Title
        context.drawCenteredTextWithShadow(
            this.textRenderer, 
            this.title, 
            this.width / 2, 
            10, 
            0xFFFFFF
        );
        
        // Labels
        context.drawTextWithShadow(this.textRenderer, "§7Min Y:", this.width / 2 - 150, 95, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, "§7Max Y:", this.width / 2 + 10, 95, 0xFFFFFF);
        
        // Estimated time
        int totalBlocks = calculateTotalBlocks();
        int blocksPerSec = blocksPerSecSlider.getValueInt();
        int estimatedSeconds = totalBlocks / blocksPerSec;
        
        String timeText = String.format("§7Dự kiến: §e%,d blocks §7trong §e%d giây", totalBlocks, estimatedSeconds);
        context.drawCenteredTextWithShadow(this.textRenderer, timeText, this.width / 2, this.height - 55, 0xFFFFFF);
        
        // Block selection checkboxes
        renderBlockCheckboxes(context, mouseX, mouseY);
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    /**
     * Render checkboxes cho từng loại block
     */
    private void renderBlockCheckboxes(DrawContext context, int mouseX, int mouseY) {
        int startX = this.width / 2 - 150;
        int startY = 150;
        int y = startY - scrollOffset;
        
        // Scrollable area
        context.enableScissor(startX - 5, startY - 5, startX + 305, this.height - 70);
        
        // Ores
        context.drawTextWithShadow(this.textRenderer, "§6§lKHOÁNG SẢN:", startX, y, 0xFFFFFF);
        y += 15;
        
        for (var entry : BlockTargets.ALL_ORES.entrySet()) {
            if (entry.getValue() == null) continue;
            
            boolean selected = targets.isSelected(entry.getValue());
            int color = selected ? 0x00FF00 : 0x888888;
            String checkbox = selected ? "[§a✓§r] " : "[ ] ";
            
            context.drawTextWithShadow(
                this.textRenderer, 
                checkbox + entry.getKey(), 
                startX + 10, 
                y, 
                color
            );
            
            // Click detection
            if (mouseX >= startX && mouseX <= startX + 300 && mouseY >= y && mouseY <= y + 10) {
                context.fill(startX, y - 1, startX + 300, y + 11, 0x33FFFFFF);
            }
            
            y += 12;
        }
        
        y += 10;
        
        // Spawners
        context.drawTextWithShadow(this.textRenderer, "§c§lSPAWNERS:", startX, y, 0xFFFFFF);
        y += 15;
        
        for (var entry : BlockTargets.ALL_SPAWNERS.entrySet()) {
            if (entry.getValue() == null) continue;
            
            boolean selected = targets.isSelected(entry.getValue());
            int color = selected ? 0x00FF00 : 0x888888;
            String checkbox = selected ? "[§a✓§r] " : "[ ] ";
            
            context.drawTextWithShadow(
                this.textRenderer, 
                checkbox + entry.getKey(), 
                startX + 10, 
                y, 
                color
            );
            
            if (mouseX >= startX && mouseX <= startX + 300 && mouseY >= y && mouseY <= y + 10) {
                context.fill(startX, y - 1, startX + 300, y + 11, 0x33FFFFFF);
            }
            
            y += 12;
        }
        
        context.disableScissor();
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Handle checkbox clicks
        int startX = this.width / 2 - 150;
        int startY = 150 - scrollOffset;
        int y = startY + 15;
        
        // Check ore clicks
        for (var entry : BlockTargets.ALL_ORES.entrySet()) {
            if (entry.getValue() == null) continue;
            
            if (mouseX >= startX && mouseX <= startX + 300 && mouseY >= y && mouseY <= y + 10) {
                targets.toggleBlock(entry.getValue());
                return true;
            }
            y += 12;
        }
        
        y += 25; // Skip spawner header
        
        // Check spawner clicks
        for (var entry : BlockTargets.ALL_SPAWNERS.entrySet()) {
            if (entry.getValue() == null) continue;
            
            if (mouseX >= startX && mouseX <= startX + 300 && mouseY >= y && mouseY <= y + 10) {
                targets.toggleBlock(entry.getValue());
                return true;
            }
            y += 12;
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollOffset -= (int) (verticalAmount * 10);
        scrollOffset = Math.max(0, Math.min(scrollOffset, 300));
        return true;
    }
    
    /**
     * Bắt đầu quét
     */
    private void startScan() {
        if (targets.getSelectedCount() == 0) {
            if (client != null && client.player != null) {
                client.player.sendMessage(Text.literal("§c[Ore Scanner] Chưa chọn block nào để quét!"), false);
            }
            return;
        }
        
        int minY = parseIntSafe(minYField.getText(), -64);
        int maxY = parseIntSafe(maxYField.getText(), 320);
        
        ScanningTask task = new ScanningTask(
            targets.getSelectedBlocks(),
            chunkRadiusSlider.getValueInt(),
            minY,
            maxY,
            blocksPerSecSlider.getValueInt()
        );
        
        if (client != null) {
            task.initialize(client);
            OreScannerClient.startScan(task);
            this.close();
            
            if (client.player != null) {
                client.player.sendMessage(Text.literal("§a[Ore Scanner] Bắt đầu quét..."), false);
            }
        }
    }
    
    private int calculateTotalBlocks() {
        int radius = chunkRadiusSlider.getValueInt();
        int minY = parseIntSafe(minYField.getText(), -64);
        int maxY = parseIntSafe(maxYField.getText(), 320);
        
        int chunks = (radius * 2 + 1) * (radius * 2 + 1);
        int yRange = maxY - minY + 1;
        
        return chunks * 16 * 16 * yRange;
    }
    
    private int parseIntSafe(String text, int defaultValue) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    @Override
    public boolean shouldPause() {
        return false;
    }

}
