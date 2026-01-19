package com.orescanner.client.gui;

import com.orescanner.client.OreScannerClient;
import com.orescanner.scanner.BlockTargets;
import com.orescanner.scanner.ScanningTask;
import net.minecraft.block.Block;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import java.util.Set;

public class ScanConfigScreen extends Screen {
    private final BlockTargets targets = new BlockTargets();
    private SliderWidget chunkRadiusSlider;
    private SliderWidget blocksPerSecSlider;
    private TextFieldWidget minYField;
    private TextFieldWidget maxYField;
    private int scrollOffset = 0;

    public ScanConfigScreen() {
        super(Text.literal("Cấu hình Ore Scanner"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = 30;

        this.chunkRadiusSlider = new SliderWidget(centerX - 150, startY, 300, 20, 
                Text.literal("Bán kính: "), Text.literal(" chunks"), 1, 12, 5, this.textRenderer);
        this.addDrawableChild(chunkRadiusSlider);
        startY += 25;

        this.blocksPerSecSlider = new SliderWidget(centerX - 150, startY, 300, 20, 
                Text.literal("Tốc độ: "), Text.literal(" blocks/s"), 1000, 500000, 50000, this.textRenderer);
        this.addDrawableChild(blocksPerSecSlider);
        startY += 35;

        this.minYField = new TextFieldWidget(this.textRenderer, centerX - 150, startY, 140, 20, Text.literal("Min Y"));
        this.minYField.setText("-64");
        this.addDrawableChild(minYField);

        this.maxYField = new TextFieldWidget(this.textRenderer, centerX + 10, startY, 140, 20, Text.literal("Max Y"));
        this.maxYField.setText("320");
        this.addDrawableChild(maxYField);
        startY += 30;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("§a§lBẮT ĐẦU QUÉT"), btn -> startScan())
                .dimensions(centerX - 100, this.height - 35, 200, 25).build());
    }

    private void startScan() {
        if (targets.getSelectedCount() == 0) return;
        try {
            int minY = Integer.parseInt(minYField.getText());
            int maxY = Integer.parseInt(maxYField.getText());
            int bps = blocksPerSecSlider.getValueInt();
            int radius = chunkRadiusSlider.getValueInt();
            
            // 1. Chuyển đổi BPS sang Blocks Per Tick (1s = 20 ticks)
            int bpt = Math.max(1, bps / 20);
            
            // 2. Lấy bản sao Set<Block> từ targets (Sửa lỗi clone và lỗi kiểu dữ liệu)
            Set<Block> selectedSet = targets.getSelectedBlocksCopy();
            
            // 3. Khởi tạo Task với đúng thứ tự tham số (Set đứng đầu theo lỗi compiler)
            ScanningTask task = new ScanningTask(selectedSet, radius, bpt, minY, maxY);
            
            OreScannerClient.startScan(task);
            this.close();
        } catch (Exception e) {
            System.err.println("Lỗi khởi tạo scan: " + e.getMessage());
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        // Vẽ danh sách block ở đây (tương tự code cũ của bạn)
        super.render(context, mouseX, mouseY, delta);
    }
}
