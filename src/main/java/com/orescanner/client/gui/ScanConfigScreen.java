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
 * Menu cấu hình scan - Đã sửa lỗi hiển thị danh sách block
 */
public class ScanConfigScreen extends Screen {
    private final BlockTargets targets = new BlockTargets();
    private SliderWidget chunkRadiusSlider;
    private SliderWidget blocksPerSecSlider;
    private TextFieldWidget minYField;
    private TextFieldWidget maxYField;
    private ButtonWidget startButton;
    private int scrollOffset = 0;

    public ScanConfigScreen() {
        super(Text.literal("Ore Scanner Configuration"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = 30;

        // Bán kính (1-12)
        this.chunkRadiusSlider = new SliderWidget(centerX - 150, startY, 300, 20, 
                Text.literal("Bán kính: "), Text.literal(" chunks"), 1, 12, 5, this.textRenderer);
        this.addDrawableChild(chunkRadiusSlider);
        startY += 25;

        // Tốc độ (1,000 - 500,000)
        this.blocksPerSecSlider = new SliderWidget(centerX - 150, startY, 300, 20, 
                Text.literal("Tốc độ: "), Text.literal(" blocks/s"), 1000, 500000, 50000, this.textRenderer);
        this.addDrawableChild(blocksPerSecSlider);
        startY += 35;

        // Min Y / Max Y
        this.minYField = new TextFieldWidget(this.textRenderer, centerX - 150, startY, 140, 20, Text.literal("Min Y"));
        this.minYField.setText("-64");
        this.addDrawableChild(minYField);

        this.maxYField = new TextFieldWidget(this.textRenderer, centerX + 10, startY, 140, 20, Text.literal("Max Y"));
        this.maxYField.setText("320");
        this.addDrawableChild(maxYField);
        startY += 30;

        // Các nút chọn nhanh
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Chọn tất cả"), btn -> targets.selectAll())
                .dimensions(centerX - 150, startY, 95, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Bỏ chọn"), btn -> targets.clearAll())
                .dimensions(centerX - 50, startY, 95, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Chỉ Kim Cương"), btn -> {
            targets.clearAll();
            targets.toggleBlock(net.minecraft.block.Blocks.DIAMOND_ORE);
            targets.toggleBlock(net.minecraft.block.Blocks.DEEPSLATE_DIAMOND_ORE);
        }).dimensions(centerX + 50, startY, 100, 20).build());

        // Nút Start
        this.startButton = ButtonWidget.builder(Text.literal("§a§lBẮT ĐẦU QUÉT"), btn -> startScan())
                .dimensions(centerX - 100, this.height - 35, 200, 25).build();
        this.addDrawableChild(startButton);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 1. Vẽ nền mờ của Minecraft
        this.renderBackground(context, mouseX, mouseY, delta);

        // 2. Vẽ danh sách block (Phải vẽ TRƯỚC super.render để các nút đè lên nếu cần)
        renderBlockCheckboxes(context, mouseX, mouseY);

        // 3. Vẽ các widget (nút, textfield, sliders)
        super.render(context, mouseX, mouseY, delta);

        // 4. Vẽ tiêu đề và các nhãn chữ trên cùng
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 8, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, "§7Min Y", this.width / 2 - 150, 92, 0xAAAAAA);
        context.drawTextWithShadow(this.textRenderer, "§7Max Y", this.width / 2 + 10, 92, 0xAAAAAA);

        // Hiển thị dự tính
        int totalBlocks = calculateTotalBlocks();
        int blocksPerSec = blocksPerSecSlider.getValueInt();
        String info = String.format("§7Dự kiến: §e%,d blocks §7| §e%d giây", totalBlocks, totalBlocks / blocksPerSec);
        context.drawCenteredTextWithShadow(this.textRenderer, info, this.width / 2, this.height - 50, 0xFFFFFF);
    }

    private void renderBlockCheckboxes(DrawContext context, int mouseX, int mouseY) {
        int startX = this.width / 2 - 150;
        int startY = 155; // Bắt đầu vùng danh sách
        int limitY = this.height - 60; // Kết thúc vùng danh sách

        // Bật Scissor để cắt những dòng tràn ra ngoài vùng cuộn
        context.enableScissor(startX, startY, startX + 300, limitY);
        
        int y = startY - scrollOffset;

        // Vẽ Khoáng sản
        context.drawTextWithShadow(this.textRenderer, "§6§lKHOÁNG SẢN:", startX, y, 0xFFFFFF);
        y += 15;
        for (var entry : BlockTargets.ALL_ORES.entrySet()) {
            renderEntry(context, entry.getKey(), entry.getValue(), startX + 10, y, mouseX, mouseY);
            y += 12;
        }

        y += 10;
        // Vẽ Spawners
        context.drawTextWithShadow(this.textRenderer, "§c§lSPAWNERS:", startX, y, 0xFFFFFF);
        y += 15;
        for (var entry : BlockTargets.ALL_SPAWNERS.entrySet()) {
            renderEntry(context, entry.getKey(), entry.getValue(), startX + 10, y, mouseX, mouseY);
            y += 12;
        }

        context.disableScissor();
    }

    private void renderEntry(DrawContext context, String name, net.minecraft.block.Block block, int x, int y, int mouseX, int mouseY) {
        if (block == null) return;
        boolean selected = targets.isSelected(block);
        String text = (selected ? "§a[✓] §f" : "§8[ ] §7") + name;
        
        // Highlight khi di chuột vào
        if (mouseX >= x && mouseX <= x + 280 && mouseY >= y && mouseY <= y + 10) {
            context.fill(x - 5, y - 1, x + 285, y + 11, 0x22FFFFFF);
        }
        
        context.drawTextWithShadow(this.textRenderer, text, x, y, 0xFFFFFF);
    }

    private int calculateTotalBlocks() {
        try {
            int r = chunkRadiusSlider.getValueInt();
            int h = Math.abs(Integer.parseInt(maxYField.getText()) - Integer.parseInt(minYField.getText()));
            return (r * 2 * 16) * (r * 2 * 16) * h;
        } catch (Exception e) { return 0; }
    }

    private void startScan() {
        if (targets.getSelectedCount() == 0) return;
        try {
            int minY = Integer.parseInt(minYField.getText());
            int maxY = Integer.parseInt(maxYField.getText());
            int bps = blocksPerSecSlider.getValueInt();
            int radius = chunkRadiusSlider.getValueInt();
            
            OreScannerClient.startScan(new ScanningTask(radius, bps, minY, maxY, targets.clone()));
            this.close();
        } catch (Exception ignored) {}
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollOffset = Math.max(0, Math.min(scrollOffset + (int)(-verticalAmount * 15), 500));
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Xử lý click chọn block (tương tự như cũ nhưng đã đồng bộ với render mới)
        int startX = this.width / 2 - 140;
        int y = 155 - scrollOffset + 15; // Bỏ qua tiêu đề "KHOÁNG SẢN"

        for (var entry : BlockTargets.ALL_ORES.entrySet()) {
            if (mouseX >= startX && mouseX <= startX + 280 && mouseY >= y && mouseY <= y + 10) {
                targets.toggleBlock(entry.getValue());
                return true;
            }
            y += 12;
        }
        // Thêm click cho Spawner tương tự...
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
